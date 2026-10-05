package com.example.managesystem.controller;

import com.example.managesystem.common.ApiResponse;
import com.example.managesystem.service.FileService;
import com.example.managesystem.vo.FileListItem;
import com.example.managesystem.vo.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Validated
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('R_SUPER') or hasAuthority('FileManage:read')")
    public ApiResponse<PageResponse<FileListItem>> page(
            @RequestParam(defaultValue = "1") @Min(1) long current,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String originalName,
            @RequestParam(required = false) String contentType
    ) {
        return ApiResponse.success(fileService.page(current, size, originalName, contentType));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('R_SUPER') or hasAuthority('FileManage:upload')")
    public ApiResponse<FileListItem> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success("文件上传成功", fileService.upload(file));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('R_SUPER') or hasAuthority('FileManage:download')")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        FileService.FileDownload download = fileService.download(id);
        String contentDisposition = ContentDisposition.attachment()
                .filename(download.metadata().getOriginalName(), StandardCharsets.UTF_8)
                .build()
                .toString();
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(download.metadata().getContentType());
        } catch (IllegalArgumentException exception) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(download.metadata().getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(download.resource());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('R_SUPER') or hasAuthority('FileManage:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return ApiResponse.success();
    }
}
