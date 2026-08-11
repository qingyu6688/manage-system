package com.example.managesystem.service;

import com.example.managesystem.common.BusinessException;
import com.example.managesystem.service.impl.AvatarStorageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvatarStorageServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void shouldStoreAndLoadValidPngAvatar() {
        AvatarStorageService service = new AvatarStorageServiceImpl(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x01}
        );

        AvatarStorageService.StoredAvatar storedAvatar = service.store(file);
        AvatarStorageService.AvatarFile loadedAvatar = service.load(storedAvatar.storageName());

        assertTrue(storedAvatar.url().startsWith("/api/profile/avatar/"));
        assertEquals("image/png", loadedAvatar.contentType());
        assertTrue(loadedAvatar.resource().exists());
    }

    @Test
    void shouldRejectImageWithInvalidSignature() {
        AvatarStorageService service = new AvatarStorageServiceImpl(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "not-an-image".getBytes()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> service.store(file));

        assertEquals("头像文件内容与格式不一致", exception.getMessage());
    }

    @Test
    void shouldRejectAvatarLargerThanTwoMegabytes() {
        AvatarStorageService service = new AvatarStorageServiceImpl(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[2 * 1024 * 1024 + 1]
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> service.store(file));

        assertEquals("头像大小不能超过2MB", exception.getMessage());
    }
}
