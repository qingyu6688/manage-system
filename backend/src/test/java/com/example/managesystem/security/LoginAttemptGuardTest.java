package com.example.managesystem.security;

import com.example.managesystem.common.BusinessException;
import com.example.managesystem.common.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptGuardTest {

    private static final String ADDRESS = "10.0.0.8";

    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-05T01:00:00Z"));
    private final LoginAttemptGuard guard = new LoginAttemptGuard(clock);

    @Test
    void locksAfterMaxFailuresAndUnlocksAfterLockDuration() {
        failTimes("admin", LoginAttemptGuard.MAX_FAILURES - 1);
        assertDoesNotThrow(() -> guard.checkAllowed("admin", ADDRESS));

        guard.recordFailure("admin", ADDRESS);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> guard.checkAllowed("admin", ADDRESS));
        assertEquals(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS, exception.getErrorCode());

        clock.advance(LoginAttemptGuard.LOCK_DURATION);
        assertDoesNotThrow(() -> guard.checkAllowed("admin", ADDRESS));
    }

    @Test
    void lockIsScopedToAccountAndAddress() {
        failTimes("admin", LoginAttemptGuard.MAX_FAILURES);

        assertThrows(BusinessException.class, () -> guard.checkAllowed(" ADMIN ", ADDRESS));
        assertDoesNotThrow(() -> guard.checkAllowed("admin", "10.0.0.9"));
        assertDoesNotThrow(() -> guard.checkAllowed("operator", ADDRESS));
    }

    @Test
    void successfulLoginClearsFailureCount() {
        failTimes("admin", LoginAttemptGuard.MAX_FAILURES - 1);
        guard.recordSuccess("admin", ADDRESS);
        failTimes("admin", LoginAttemptGuard.MAX_FAILURES - 1);

        assertDoesNotThrow(() -> guard.checkAllowed("admin", ADDRESS));
    }

    @Test
    void failuresOutsideWindowStartNewCount() {
        failTimes("admin", LoginAttemptGuard.MAX_FAILURES - 1);
        clock.advance(LoginAttemptGuard.LOCK_DURATION.plusSeconds(1));
        guard.recordFailure("admin", ADDRESS);

        assertDoesNotThrow(() -> guard.checkAllowed("admin", ADDRESS));
    }

    private void failTimes(String username, int times) {
        for (int i = 0; i < times; i++) {
            guard.recordFailure(username, ADDRESS);
        }
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        private MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
