package com.example.managesystem.service;

import com.example.managesystem.common.BusinessException;
import com.example.managesystem.entity.SysFile;
import com.example.managesystem.mapper.SysFileMapper;
import com.example.managesystem.security.LoginUser;
import com.example.managesystem.service.impl.FileServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private SysFileMapper fileMapper;

    @TempDir
    private Path storageRoot;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl(fileMapper, storageRoot.toString());
        LoginUser loginUser = new LoginUser(1L, "admin", "password", true, List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldUploadAllowedFileWithRandomStorageName() throws Exception {
        doAnswer(invocation -> {
            SysFile file = invocation.getArgument(0);
            file.setId(12L);
            return 1;
        }).when(fileMapper).insert(any(SysFile.class));

        var result = fileService.upload(new MockMultipartFile(
                "file",
                "../说明.txt",
                "text/plain",
                "测试内容".getBytes()
        ));

        ArgumentCaptor<SysFile> captor = ArgumentCaptor.forClass(SysFile.class);
        verify(fileMapper).insert(captor.capture());
        SysFile saved = captor.getValue();
        assertEquals(12L, result.id());
        assertEquals("说明.txt", saved.getOriginalName());
        assertEquals("admin", saved.getUploader());
        assertFalse(saved.getStorageName().contains(".."));
        assertTrue(Files.isRegularFile(storageRoot.resolve(saved.getStorageName())));
    }

    @Test
    void shouldRejectExecutableFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "unsafe.exe",
                "text/plain",
                new byte[]{1, 2, 3}
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fileService.upload(file)
        );

        assertEquals("不支持该文件扩展名", exception.getMessage());
    }

    @Test
    void shouldDeletePhysicalFileAndMetadata() throws Exception {
        SysFile file = new SysFile();
        file.setId(8L);
        file.setStorageName("safe-file.txt");
        Path physicalFile = storageRoot.resolve(file.getStorageName());
        Files.writeString(physicalFile, "content");
        when(fileMapper.selectById(8L)).thenReturn(file);

        fileService.delete(8L);

        verify(fileMapper).deleteById(8L);
        assertFalse(Files.exists(physicalFile));
    }
}
