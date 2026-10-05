package com.example.managesystem.integration;

import com.example.managesystem.dto.AnnouncementStatusRequest;
import com.example.managesystem.dto.PasswordChangeRequest;
import com.example.managesystem.entity.SysUser;
import com.example.managesystem.mapper.SysUserMapper;
import com.example.managesystem.security.CustomUserDetailsService;
import com.example.managesystem.service.AnnouncementService;
import com.example.managesystem.service.ProfileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:security-flow;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
        "spring.datasource.password=", "spring.flyway.enabled=false",
        "security.jwt.secret=integration-test-secret-only-at-least-32-bytes",
        "logging.level.com.example.managesystem=OFF"
})
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
class SecurityFlowTest {
    private static final boolean MYSQL = Boolean.getBoolean("integration.mysql");

    @org.springframework.test.context.DynamicPropertySource
    static void database(org.springframework.test.context.DynamicPropertyRegistry registry) {
        if (MYSQL) {
            // 仅连接专用临时实例，禁止通过参数指向既有业务库。
            registry.add("spring.datasource.url", () -> "jdbc:mysql://127.0.0.1:33178/manage_system_test_isolated?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai");
            registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
            registry.add("spring.datasource.username", () -> "root");
            registry.add("spring.datasource.password", () -> "");
        }
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;
    @Autowired AnnouncementService announcements;
    @Autowired ProfileService profiles;
    @Autowired CustomUserDetailsService users;
    @Autowired SysUserMapper userMapper;
    @Autowired com.example.managesystem.service.AuthService authService;

    @BeforeEach
    void schema() throws Exception {
        if (MYSQL) {
            try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
                if (!"manage_system_test_isolated".equals(connection.getCatalog())) {
                    throw new IllegalStateException("拒绝清理非隔离测试数据库");
                }
                statement.execute("SET FOREIGN_KEY_CHECKS=0");
                for (String table : jdbc.queryForList("SHOW TABLES", String.class)) {
                    if (!table.matches("[a-z_]+")) { throw new IllegalStateException("测试表名不正确"); }
                    statement.execute("DROP TABLE `" + table + "`");
                }
                statement.execute("SET FOREIGN_KEY_CHECKS=1");
            }
        } else {
            jdbc.execute("DROP ALL OBJECTS");
        }
        var resources = new PathMatchingResourcePatternResolver().getResources("classpath:db/migration/V*.sql");
        Arrays.sort(resources, Comparator.comparing(resource -> resource.getFilename()));
        var populator = new ResourceDatabasePopulator();
        populator.setSqlScriptEncoding("UTF-8");
        for (var resource : resources) {
            // 仅适配 H2 不支持的 MySQL 表选项；SQL 业务内容仍取自正式迁移。
            String sql = resource.getContentAsString(StandardCharsets.UTF_8);
            if (!MYSQL) {
                sql = sql
                    .replaceAll("(?i)\\bUNSIGNED\\b", "")
                    .replaceAll("(?i)\\) ENGINE=InnoDB[^;]*;", ");")
                    .replaceAll("(?i) AFTER [a-z_]+", "")
                    .replaceAll(",\\s*COMMENT\\s*=\\s*'[^']*'", "");
            if (resource.getFilename().startsWith("V3")) {
                sql = sql.replaceAll(",\\s*ADD COLUMN", "; ALTER TABLE sys_user ADD COLUMN");
            }
            if (resource.getFilename().startsWith("V6")) {
                sql = sql.replaceAll(",\\s*ADD", "; ALTER TABLE sys_operation_log ADD");
            }
            }
            populator.addScript(new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8)));
        }
        populator.execute(dataSource);
    }

    @Test
    void administratorCannotPromoteOrEditSuperUser() throws Exception {
        String token = login("operator").path("token").asText();
        mvc.perform(put("/api/user/1").header("Authorization", "Bearer " + token)
                .contentType("application/json").content(userBody("admin", 1)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/user").header("Authorization", "Bearer " + token)
                .contentType("application/json").content(userBody("new-super", 1)))
                .andExpect(status().isForbidden());
    }

    @Test
    @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "integration.mysql", matches = "true")
    void flywayMigratesAndValidatesUnmodifiedMysqlScripts() {
        var flyway = org.flywaydb.core.Flyway.configure().dataSource(dataSource)
                .cleanDisabled(false).load();
        flyway.clean();
        assertEquals(7, flyway.migrate().migrationsExecuted);
        flyway.validate();
        assertEquals(7, jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1", Integer.class));
    }

    @Test
    void disabledUserCannotUseAccessOrRefreshToken() throws Exception {
        JsonNode tokens = login("operator");
        jdbc.update("UPDATE sys_user SET status=2 WHERE id=2");
        assertAccessDenied(tokens);
        assertRefreshDenied(tokens);
    }

    @Test
    void passwordChangeRevokesBothTokens() throws Exception {
        JsonNode tokens = login("operator");
        profiles.changePassword(users.loadUserById(2L), new PasswordChangeRequest(
                "Admin@123", "ChangedPass123", "ChangedPass123"));
        assertAccessDenied(tokens);
        assertRefreshDenied(tokens);
    }

    @Test
    void logoutRevokesSession() throws Exception {
        JsonNode tokens = login("operator");
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + tokens.path("token").asText()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        assertAccessDenied(tokens);
        assertRefreshDenied(tokens);
    }

    @Test
    void refreshTokenIsSingleUse() throws Exception {
        JsonNode old = login("operator");
        JsonNode next = refresh(old);
        assertFalse(next.path("token").asText().isEmpty());
        assertRefreshDenied(old);
        mvc.perform(get("/api/user/list").header("Authorization", "Bearer " + next.path("token").asText()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void reusedUsernameCannotRebindToken() throws Exception {
        JsonNode tokens = login("operator");
        jdbc.update("UPDATE sys_user SET username='renamed' WHERE id=2");
        jdbc.update("UPDATE sys_user SET username='operator' WHERE id=1");
        assertAccessDenied(tokens);
        assertRefreshDenied(tokens);
    }

    @Test
    void customRoleCanReadButCannotCreateUsers() throws Exception {
        jdbc.update("INSERT INTO sys_role(id,name,code) VALUES(20,'只读测试角色','R_READER')");
        jdbc.update("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(20,10),(20,11)");
        jdbc.update("UPDATE sys_user_role SET role_id=20 WHERE user_id=2");
        JsonNode tokens = login("operator");
        String bearer = "Bearer " + tokens.path("token").asText();
        mvc.perform(get("/api/user/list").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        mvc.perform(post("/api/user").header("Authorization", bearer).contentType("application/json")
                .content(userBody("new-reader", 20))).andExpect(status().isForbidden());
    }

    @Test
    void messageSenderCanChooseRecipientsWithoutUserManagement() throws Exception {
        jdbc.update("INSERT INTO sys_role(id,name,code) VALUES(20,'消息发送测试','R_SENDER')");
        jdbc.update("INSERT INTO sys_role_menu(role_id,menu_id) VALUES(20,10),(20,80),(20,81)");
        jdbc.update("UPDATE sys_user_role SET role_id=20 WHERE user_id=2");
        String bearer = "Bearer " + login("operator").path("token").asText();
        mvc.perform(get("/api/message/receivers").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].password").doesNotExist())
                .andExpect(jsonPath("$.data[0].userEmail").doesNotExist());
        mvc.perform(get("/api/user/list").header("Authorization", bearer)).andExpect(status().isForbidden());
    }

    @Test
    void removedButtonIsEnforcedByApi() throws Exception {
        jdbc.update("DELETE FROM sys_role_menu WHERE role_id=2 AND menu_id=12");
        String token = login("operator").path("token").asText();
        mvc.perform(post("/api/user").header("Authorization", "Bearer " + token)
                .contentType("application/json").content(userBody("new-user", 2)))
                .andExpect(status().isForbidden());
    }

    @Test
    void editingUserWithoutPasswordKeepsHash() throws Exception {
        String previous = jdbc.queryForObject("SELECT password FROM sys_user WHERE id=2", String.class);
        String token = login("admin").path("token").asText();
        String body = userBody("operator", 2).replace("InitialPass123", "");
        mvc.perform(put("/api/user/2").header("Authorization", "Bearer " + token)
                .contentType("application/json").content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        assertEquals(previous, jdbc.queryForObject("SELECT password FROM sys_user WHERE id=2", String.class));
    }

    @Test
    void nullFieldsArePersistedAndDeletedNamesRemainReserved() {
        var userDetails = users.loadUserById(1L);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()));
        announcements.updateStatus(1L, new AnnouncementStatusRequest("DRAFT"));
        assertNull(jdbc.queryForObject("SELECT published_at FROM sys_announcement WHERE id=1", Object.class));
        assertNull(jdbc.queryForObject("SELECT published_by FROM sys_announcement WHERE id=1", String.class));
        SysUser user = userMapper.selectById(2L);
        user.setPhone(null);
        user.setBio(null);
        userMapper.updateById(user);
        assertNull(jdbc.queryForObject("SELECT phone FROM sys_user WHERE id=2", String.class));
        userMapper.deleteById(2L);
        assertEquals(1, userMapper.countReservedName("operator", null));
    }

    @Test
    void concurrentRefreshHasExactlyOneWinner() throws Exception {
        JsonNode tokens = login("operator");
        var executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        var start = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.Callable<Boolean> attempt = () -> {
            start.await();
            try {
                authService.refresh(tokens.path("refreshToken").asText());
                return true;
            } catch (com.example.managesystem.common.BusinessException exception) {
                return false;
            }
        };
        try {
            var first = executor.submit(attempt);
            var second = executor.submit(attempt);
            start.countDown();
            assertNotEquals(first.get(10, java.util.concurrent.TimeUnit.SECONDS),
                    second.get(10, java.util.concurrent.TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void logoutDoesNotRevokeOtherDevice() throws Exception {
        JsonNode first = login("operator");
        JsonNode second = login("operator");
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + first.path("token").asText()))
                .andExpect(jsonPath("$.code").value(200));
        mvc.perform(get("/api/user/list").header("Authorization", "Bearer " + second.path("token").asText()))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void creationCannotBypassPublishPermission() throws Exception {
        jdbc.update("DELETE FROM sys_role_menu WHERE role_id=2 AND menu_id=54");
        String bearer = "Bearer " + login("operator").path("token").asText();
        mvc.perform(post("/api/announcement").header("Authorization", bearer)
                .contentType("application/json").content(json.writeValueAsString(Map.of(
                        "title", "测试公告", "content", "公告内容", "type", "SYSTEM", "status", "PUBLISHED", "top", false))))
                .andExpect(status().isForbidden());
    }

    @Test
    void clearingLogsLeavesAuditRecord() throws Exception {
        String bearer = "Bearer " + login("admin").path("token").asText();
        mvc.perform(delete("/api/log/clear").header("Authorization", bearer))
                .andExpect(jsonPath("$.code").value(200));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM sys_operation_log WHERE path='/api/log/clear'", Integer.class));
    }

    @Test
    void shortPasswordIsStillRejected() throws Exception {
        String bearer = "Bearer " + login("admin").path("token").asText();
        mvc.perform(put("/api/user/2").header("Authorization", bearer)
                .contentType("application/json").content(userBody("operator", 2).replace("InitialPass123", "short")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void staleEntityCannotRestoreRevokedTokenVersion() {
        SysUser stale = userMapper.selectById(2L);
        userMapper.revokeTokens(2L);
        stale.setNickname("更新昵称");
        userMapper.updateById(stale);
        assertEquals(1L, userMapper.selectById(2L).getTokenVersion());
    }

    @org.junit.jupiter.api.AfterEach
    void clearAuthentication() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    private JsonNode login(String username) throws Exception {
        return json.readTree(mvc.perform(post("/api/auth/login").contentType("application/json")
                .content(json.writeValueAsString(Map.of("userName", username, "password", "Admin@123"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString()).path("data");
    }

    private JsonNode refresh(JsonNode tokens) throws Exception {
        return json.readTree(mvc.perform(post("/api/auth/refresh").contentType("application/json")
                .content(json.writeValueAsString(Map.of("refreshToken", tokens.path("refreshToken").asText()))))
                .andExpect(jsonPath("$.code").value(200)).andReturn().getResponse().getContentAsString()).path("data");
    }

    private void assertAccessDenied(JsonNode tokens) throws Exception {
        mvc.perform(get("/api/user/list").header("Authorization", "Bearer " + tokens.path("token").asText()))
                .andExpect(status().isUnauthorized());
    }

    private void assertRefreshDenied(JsonNode tokens) throws Exception {
        mvc.perform(post("/api/auth/refresh").contentType("application/json")
                .content(json.writeValueAsString(Map.of("refreshToken", tokens.path("refreshToken").asText()))))
                .andExpect(jsonPath("$.code").value(401));
    }

    private String userBody(String username, long roleId) throws Exception {
        return json.writeValueAsString(Map.of("userName", username, "nickName", "测试用户",
                "password", "InitialPass123", "status", 1, "roleIds", new long[]{roleId}));
    }
}
