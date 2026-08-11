package com.example.managesystem.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface AvatarStorageService {

    StoredAvatar store(MultipartFile file);

    AvatarFile load(String storageName);

    void deleteByUrl(String avatarUrl);

    void deleteQuietly(String storageName);

    record StoredAvatar(String storageName, String url) {
    }

    record AvatarFile(Resource resource, String contentType) {
    }
}
