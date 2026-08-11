package com.example.managesystem.service;

import com.example.managesystem.entity.SysFile;
import com.example.managesystem.vo.FileListItem;
import com.example.managesystem.vo.PageResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    PageResponse<FileListItem> page(
            long current,
            long size,
            String originalName,
            String contentType
    );

    FileListItem upload(MultipartFile multipartFile);

    FileDownload download(Long id);

    void delete(Long id);

    record FileDownload(Resource resource, SysFile metadata) {
    }
}
