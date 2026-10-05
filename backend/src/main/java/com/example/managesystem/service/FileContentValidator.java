package com.example.managesystem.service;

import com.example.managesystem.common.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

public final class FileContentValidator {
    private static final Map<String, String> TYPES = Map.ofEntries(
            Map.entry("jpg", "image/jpeg"), Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"), Map.entry("gif", "image/gif"), Map.entry("webp", "image/webp"),
            Map.entry("pdf", "application/pdf"), Map.entry("txt", "text/plain"),
            Map.entry("csv", "text/csv"), Map.entry("md", "text/markdown"), Map.entry("zip", "application/zip"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            Map.entry("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"));
    private static final long MAX_EXPANDED_SIZE = 100L * 1024 * 1024;
    private static final int MAX_ZIP_ENTRIES = 4096;

    private FileContentValidator() { }

    public static void validate(MultipartFile file, String extension, String contentType) {
        boolean plainText = Set.of("csv", "md").contains(extension) && "text/plain".equals(contentType);
        if (!contentType.equals(TYPES.get(extension)) && !plainText) {
            throw new BusinessException("文件扩展名与声明类型不一致");
        }
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(8192);
            boolean valid = switch (extension) {
                case "jpg", "jpeg" -> starts(header, 0xff, 0xd8, 0xff);
                case "png" -> starts(header, 137, 80, 78, 71, 13, 10, 26, 10);
                case "gif" -> ascii(header).startsWith("GIF87a") || ascii(header).startsWith("GIF89a");
                case "webp" -> ascii(header).startsWith("RIFF") && header.length >= 12
                        && new String(header, 8, 4, StandardCharsets.US_ASCII).equals("WEBP");
                case "pdf" -> ascii(header).startsWith("%PDF-");
                case "zip", "docx", "xlsx", "pptx" -> validateArchive(file, extension);
                default -> isText(header);
            };
            if (!valid) { throw new BusinessException("文件内容与所选格式不一致"); }
        } catch (IOException exception) {
            throw new BusinessException("文件内容无法读取，请检查文件是否完整");
        }
    }

    private static boolean validateArchive(MultipartFile file, String extension) throws IOException {
        String required = switch (extension) {
            case "docx" -> "word/document.xml";
            case "xlsx" -> "xl/workbook.xml";
            case "pptx" -> "ppt/presentation.xml";
            default -> "";
        };
        boolean found = required.isEmpty();
        boolean types = required.isEmpty();
        int count = 0;
        long expanded = 0;
        byte[] buffer = new byte[8192];
        try (ZipInputStream zip = new ZipInputStream(file.getInputStream())) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (++count > MAX_ZIP_ENTRIES) { throw new BusinessException("压缩包条目过多"); }
                found |= required.equals(entry.getName());
                types |= "[Content_Types].xml".equals(entry.getName());
                int size;
                while ((size = zip.read(buffer)) != -1) {
                    expanded += size;
                    if (expanded > MAX_EXPANDED_SIZE) { throw new BusinessException("压缩包展开内容过大"); }
                }
            }
        }
        return count > 0 && found && types;
    }

    private static boolean starts(byte[] header, int... prefix) {
        if (header.length < prefix.length) { return false; }
        for (int i = 0; i < prefix.length; i++) {
            if (Byte.toUnsignedInt(header[i]) != prefix[i]) { return false; }
        }
        return true;
    }

    private static String ascii(byte[] header) {
        return new String(header, 0, Math.min(12, header.length), StandardCharsets.US_ASCII);
    }

    private static boolean isText(byte[] header) {
        for (byte value : header) {
            int unsigned = Byte.toUnsignedInt(value);
            if (unsigned < 32 && unsigned != 9 && unsigned != 10 && unsigned != 13) { return false; }
        }
        return true;
    }
}
