package com.example.managesystem.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public record PageResponse<T>(List<T> records, long current, long size, long total) {

    public static <T> PageResponse<T> from(IPage<T> page) {
        return new PageResponse<>(page.getRecords(), page.getCurrent(), page.getSize(), page.getTotal());
    }
}
