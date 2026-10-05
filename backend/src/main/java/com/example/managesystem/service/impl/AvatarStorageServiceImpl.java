package com.example.managesystem.service.impl;

import com.example.managesystem.common.BusinessException;
import com.example.managesystem.common.ErrorCode;
import com.example.managesystem.common.SystemException;
import com.example.managesystem.service.AvatarStorageService;
import com.example.managesystem.service.FileTransactions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class AvatarStorageServiceImpl implements AvatarStorageService {

    private static final long MAX_AVATAR_SIZE = 2L * 1024 * 1024;
    private static final String AVATAR_URL_PREFIX = "/api/profile/avatar/";
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final Map<String, Set<String>> CONTENT_TYPE_EXTENSIONS = Map.of(
            "image/jpeg", Set.of("jpg", "jpeg"),
            "image/png", Set.of("png"),
            "image/webp", Set.of("webp")
    );

    private final Path storageRoot;

    public AvatarStorageServiceImpl(@Value("${app.avatar.storage-root}") String storageRoot) {
        this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
    }

    public StoredAvatar store(MultipartFile file) {
        AvatarMetadata metadata = validate(file);
        String storageName = UUID.randomUUID().toString().replace("-", "")
                + "."
                + metadata.extension();
        Path target = resolve(storageName);
        try {
            Files.createDirectories(storageRoot);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            deleteQuietly(storageName);
            throw new SystemException(ErrorCode.FILE_STORAGE_ERROR, "头像保存失败，请稍后重试", exception);
        }
        FileTransactions.onRollback(() -> deleteQuietly(storageName));
        return new StoredAvatar(storageName, AVATAR_URL_PREFIX + storageName);
    }

    public AvatarFile load(String storageName) {
        Path path = resolve(storageName);
        if (!Files.isRegularFile(path)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "头像不存在或已被移除");
        }
        return new AvatarFile(new FileSystemResource(path), contentType(storageName));
    }

    public void deleteByUrl(String avatarUrl) {
        if (!StringUtils.hasText(avatarUrl) || !avatarUrl.startsWith(AVATAR_URL_PREFIX)) {
            return;
        }
        deleteQuietly(avatarUrl.substring(AVATAR_URL_PREFIX.length()));
    }

    public void deleteQuietly(String storageName) {
        try {
            Files.deleteIfExists(resolve(storageName));
        } catch (BusinessException | IOException exception) {
            // 清理旧头像失败不应影响已完成的资料更新，后续可通过运维任务清理孤立文件。
            log.warn("头像文件清理失败 storageName={} message={}", storageName, exception.getMessage());
        }
    }

    private AvatarMetadata validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的头像");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException("头像大小不能超过2MB");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            throw new BusinessException("无法识别头像文件类型");
        }
        contentType = contentType.toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException("头像仅支持JPG、PNG或WebP格式");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (!StringUtils.hasText(extension)) {
            throw new BusinessException("头像文件缺少扩展名");
        }
        extension = extension.toLowerCase(Locale.ROOT);
        if (!CONTENT_TYPE_EXTENSIONS.get(contentType).contains(extension)) {
            throw new BusinessException("头像扩展名与文件类型不一致");
        }

        if (!hasExpectedSignature(file, contentType)) {
            throw new BusinessException("头像文件内容与格式不一致");
        }
        return new AvatarMetadata(contentType, extension);
    }

    private boolean hasExpectedSignature(MultipartFile file, String contentType) {
        byte[] header;
        try (InputStream inputStream = file.getInputStream()) {
            header = inputStream.readNBytes(12);
        } catch (IOException exception) {
            throw new SystemException(ErrorCode.FILE_STORAGE_ERROR, "头像文件读取失败", exception);
        }
        return switch (contentType) {
            case "image/jpeg" -> header.length >= 3
                    && unsigned(header[0]) == 0xFF
                    && unsigned(header[1]) == 0xD8
                    && unsigned(header[2]) == 0xFF;
            case "image/png" -> header.length >= 8
                    && unsigned(header[0]) == 0x89
                    && unsigned(header[1]) == 0x50
                    && unsigned(header[2]) == 0x4E
                    && unsigned(header[3]) == 0x47
                    && unsigned(header[4]) == 0x0D
                    && unsigned(header[5]) == 0x0A
                    && unsigned(header[6]) == 0x1A
                    && unsigned(header[7]) == 0x0A;
            case "image/webp" -> header.length >= 12
                    && header[0] == 'R'
                    && header[1] == 'I'
                    && header[2] == 'F'
                    && header[3] == 'F'
                    && header[8] == 'W'
                    && header[9] == 'E'
                    && header[10] == 'B'
                    && header[11] == 'P';
            default -> false;
        };
    }

    private int unsigned(byte value) {
        return value & 0xFF;
    }

    private Path resolve(String storageName) {
        if (!StringUtils.hasText(storageName)
                || !storageName.matches("(?i)[a-f0-9]{32}\\.(jpg|jpeg|png|webp)")) {
            throw new BusinessException("头像文件名不正确");
        }
        Path resolved = storageRoot.resolve(storageName).toAbsolutePath().normalize();
        if (!resolved.startsWith(storageRoot)) {
            throw new BusinessException("头像路径不安全");
        }
        return resolved;
    }

    private String contentType(String storageName) {
        String extension = StringUtils.getFilenameExtension(storageName);
        if ("png".equalsIgnoreCase(extension)) {
            return "image/png";
        }
        if ("webp".equalsIgnoreCase(extension)) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    private record AvatarMetadata(String contentType, String extension) {
    }
}
