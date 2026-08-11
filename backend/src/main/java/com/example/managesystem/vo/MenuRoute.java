package com.example.managesystem.vo;

import java.util.ArrayList;
import java.util.List;

public record MenuRoute(
        Long id,
        Long parentId,
        String path,
        String name,
        String component,
        MenuMeta meta,
        List<MenuRoute> children
) {
    public record MenuMeta(
            String title,
            String icon,
            Boolean isHide,
            Boolean keepAlive,
            Boolean isHideTab,
            String link,
            Boolean isIframe,
            Boolean showBadge,
            String showTextBadge,
            Boolean fixedTab,
            String activePath,
            Boolean isFullPage,
            Boolean isEnable,
            Integer sort,
            List<AuthItem> authList
    ) {
    }

    public record AuthItem(Long id, String title, String authMark, Boolean enabled, Integer sort) {
    }

    public static List<MenuRoute> emptyChildren() {
        return new ArrayList<>();
    }
}
