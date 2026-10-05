package com.example.managesystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.managesystem.common.BusinessException;
import com.example.managesystem.common.ErrorCode;
import com.example.managesystem.common.SystemException;
import com.example.managesystem.entity.SysFile;
import com.example.managesystem.mapper.SysFileMapper;
import com.example.managesystem.security.SecurityUtils;
import com.example.managesystem.service.FileService;
import com.example.managesystem.service.FileTransactions;
import com.example.managesystem.service.FileContentValidator;
import com.example.managesystem.vo.FileListItem;
import com.example.managesystem.vo.PageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "application/pdf",
            "text/plain",
            "text/csv",
            "text/markdown",
            "application/zip",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "pdf", "txt", "csv", "md", "zip",
            "docx", "xlsx", "pptx"
    );

    private final SysFileMapper fileMapper;
    private final Path storageRoot;

    public FileServiceImpl(
            SysFileMapper fileMapper,
            @Value("${app.file.storage-root}") String storageRoot
    ) {
        this.fileMapper = fileMapper;
        this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
    }

    public PageResponse<FileListItem> page(
            long current,
            long size,
            String originalName,
            String contentType
    ) {
        long safeSize = Math.min(Math.max(size, 1), 100);
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<SysFile>()
                .like(StringUtils.hasText(originalName), SysFile::getOriginalName, originalName)
                .like(StringUtils.hasText(contentType), SysFile::getContentType, contentType)
                .orderByDesc(SysFile::getCreatedAt);
        IPage<SysFile> page = fileMapper.selectPage(
                new Page<>(Math.max(current, 1), safeSize),
                wrapper
        );
        return PageResponse.from(page.convert(this::toListItem));
    }

    @Transactional
    public FileListItem upload(MultipartFile multipartFile) {
        validateFile(multipartFile);
        String originalName = normalizeOriginalName(multipartFile.getOriginalFilename());
        String storageName = createStorageName(originalName);
        Path target = resolveStoragePath(storageName);
        try {
            Files.createDirectories(storageRoot);
            try (var input = multipartFile.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            deletePhysicalFile(target);
            throw new SystemException(ErrorCode.FILE_STORAGE_ERROR, "文件保存失败，请稍后重试", exception);
        }

        FileTransactions.onRollback(() -> deletePhysicalFile(target));
        try {
            LocalDateTime now = LocalDateTime.now();
            SysFile file = new SysFile();
            file.setOriginalName(originalName);
            file.setStorageName(storageName);
            file.setContentType(multipartFile.getContentType().toLowerCase(Locale.ROOT));
            file.setFileSize(multipartFile.getSize());
            file.setUploader(SecurityUtils.currentUsername());
            file.setCreatedAt(now);
            file.setUpdatedAt(now);
            fileMapper.insert(file);
            return toListItem(file);
        } catch (RuntimeException exception) {
            deletePhysicalFile(target);
            throw exception;
        }
    }

    public FileDownload download(Long id) {
        SysFile file = requireFile(id);
        Path path = resolveStoragePath(file.getStorageName());
        if (!Files.isRegularFile(path)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "文件不存在或已被移除");
        }
        return new FileDownload(new FileSystemResource(path), file);
    }

    @Transactional
    public void delete(Long id) {
        SysFile file = requireFile(id);
        Path path = resolveStoragePath(file.getStorageName());
        fileMapper.deleteById(id);
        FileTransactions.afterCommit(() -> deletePhysicalFile(path));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过20MB");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)
                || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("不支持该文件类型");
        }
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (!StringUtils.hasText(extension)
                || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("不支持该文件扩展名");
        }
        FileContentValidator.validate(file, extension.toLowerCase(Locale.ROOT), contentType.toLowerCase(Locale.ROOT));
    }

    private String normalizeOriginalName(String originalName) {
        if (!StringUtils.hasText(originalName)) {
            throw new BusinessException("文件名不能为空");
        }
        String normalized = originalName.replace('\\', '/');
        normalized = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        if (!StringUtils.hasText(normalized) || normalized.length() > 255) {
            throw new BusinessException("文件名不正确或过长");
        }
        return normalized;
    }

    private String createStorageName(String originalName) {
        String extension = StringUtils.getFilenameExtension(originalName);
        if (!StringUtils.hasText(extension) || !extension.matches("[A-Za-z0-9]{1,10}")) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        return UUID.randomUUID().toString().replace("-", "")
                + "."
                + extension.toLowerCase(Locale.ROOT);
    }

    private Path resolveStoragePath(String storageName) {
        Path resolved = storageRoot.resolve(storageName).toAbsolutePath().normalize();
        if (!resolved.startsWith(storageRoot)) {
            throw new BusinessException("文件路径不安全");
        }
        return resolved;
    }

    private void deletePhysicalFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new SystemException(ErrorCode.FILE_STORAGE_ERROR, "文件清理失败，存储文件：" + path.getFileName(), exception);
        }
    }

    private SysFile requireFile(Long id) {
        SysFile file = fileMapper.selectById(id);
        if (file == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "文件记录不存在");
        }
        return file;
    }

    private FileListItem toListItem(SysFile file) {
        return new FileListItem(
                file.getId(),
                file.getOriginalName(),
                file.getContentType(),
                file.getFileSize(),
                file.getUploader(),
                file.getCreatedAt()
        );
    }
}
