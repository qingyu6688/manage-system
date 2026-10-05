package com.example.managesystem.security;

import com.example.managesystem.common.BusinessException;
import com.example.managesystem.common.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败限流：同一账号在同一来源地址连续失败达到上限后，锁定期内直接拒绝。
 * <p>
 * 按“账号 + 来源地址”计数，避免攻击者仅凭用户名就把管理员锁死。
 * 状态保存在进程内存中，只对单实例部署有效，多实例部署需要换成共享存储。
 */
@Component
public class LoginAttemptGuard {

    static final int MAX_FAILURES = 5;
    static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    /** 超过该数量时先清理过期记录，防止伪造大量用户名撑爆内存。 */
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptGuard() {
        this(Clock.systemUTC());
    }

    LoginAttemptGuard(Clock clock) {
        this.clock = clock;
    }

    public void checkAllowed(String username, String clientAddress) {
        Attempt attempt = attempts.get(key(username, clientAddress));
        if (attempt != null && attempt.lockedAt(clock.instant())) {
            throw new BusinessException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS,
                    "登录失败次数过多，请" + LOCK_DURATION.toMinutes() + "分钟后再试");
        }
    }

    public void recordFailure(String username, String clientAddress) {
        Instant now = clock.instant();
        if (attempts.size() >= MAX_TRACKED_KEYS) {
            attempts.values().removeIf(attempt -> attempt.expiredAt(now));
        }
        attempts.compute(key(username, clientAddress), (ignored, previous) ->
                previous == null || previous.expiredAt(now) ? new Attempt(1, now) : previous.increment(now));
    }

    public void recordSuccess(String username, String clientAddress) {
        attempts.remove(key(username, clientAddress));
    }

    private String key(String username, String clientAddress) {
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        return normalized + "|" + (clientAddress == null ? "" : clientAddress);
    }

    /** 记录窗口内的失败次数和最近一次失败时间，锁定期从最近一次失败开始计算。 */
    private record Attempt(int failures, Instant lastFailure) {

        Attempt increment(Instant now) {
            return new Attempt(failures + 1, now);
        }

        boolean lockedAt(Instant now) {
            return failures >= MAX_FAILURES && !expiredAt(now);
        }

        boolean expiredAt(Instant now) {
            return !now.isBefore(lastFailure.plus(LOCK_DURATION));
        }
    }
}
