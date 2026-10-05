package com.example.managesystem.service;

import com.example.managesystem.common.BusinessException;
import com.example.managesystem.entity.SysUser;
import com.example.managesystem.mapper.SysUserMapper;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.service.impl.AvatarStorageServiceImpl;
import com.example.managesystem.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileSafetyTest {
    @TempDir Path root;

    @AfterEach
    void clearTransaction() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void rollbackCleansNewAvatarAndKeepsOldFile() throws Exception {
        var mapper = mock(SysUserMapper.class);
        var storage = new AvatarStorageServiceImpl(root.toString());
        var profile = new ProfileServiceImpl(mapper, mock(PasswordEncoder.class), storage);
        String oldName = "a".repeat(32) + ".png";
        Path oldFile = Files.writeString(root.resolve(oldName), "旧头像");
        SysUser user = new SysUser();
        user.setId(2L);
        user.setStatus(1);
        user.setAvatar("/api/profile/avatar/" + oldName);
        when(mapper.selectById(2L)).thenReturn(user);
        when(mapper.selectRoleCodesByUserId(2L)).thenThrow(new IllegalStateException("数据库暂时不可用"));
        TransactionSynchronizationManager.initSynchronization();
        var file = new MockMultipartFile("file", "new.png", "image/png",
                new byte[]{(byte) 137, 80, 78, 71, 13, 10, 26, 10});
        assertThrows(IllegalStateException.class, () -> profile.uploadAvatar(
                new LoginUser(2L, "用户", "哈希", true, List.of()), file));
        for (var callback : TransactionSynchronizationManager.getSynchronizations()) {
            callback.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        }
        assertTrue(Files.exists(oldFile));
        try (var files = Files.list(root)) { assertEquals(1, files.count()); }
    }

    @Test
    void oldFileCleanupWaitsForCommit() {
        TransactionSynchronizationManager.initSynchronization();
        var cleaned = new AtomicBoolean(false);
        FileTransactions.afterCommit(() -> cleaned.set(true));
        assertFalse(cleaned.get());
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        assertTrue(cleaned.get());
    }

    @Test
    void rejectMismatchedTypeAndDisguisedBinary() {
        var file = new MockMultipartFile("file", "report.pdf", "image/png", new byte[]{1, 0, 3});
        assertThrows(BusinessException.class, () -> FileContentValidator.validate(file, "pdf", "image/png"));
        assertThrows(BusinessException.class, () -> FileContentValidator.validate(file, "pdf", "application/pdf"));
        assertThrows(BusinessException.class, () -> FileContentValidator.validate(file, "txt", "text/plain"));
    }

    @Test
    void rejectZipDisguisedAsOfficeDocument() throws Exception {
        var bytes = new java.io.ByteArrayOutputStream();
        try (var zip = new java.util.zip.ZipOutputStream(bytes)) {
            zip.putNextEntry(new java.util.zip.ZipEntry("readme.txt"));
            zip.write("说明".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        var file = new MockMultipartFile("file", "document.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", bytes.toByteArray());
        assertThrows(BusinessException.class, () -> FileContentValidator.validate(file, "docx", file.getContentType()));
    }
}
