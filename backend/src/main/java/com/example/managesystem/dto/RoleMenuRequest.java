package com.example.managesystem.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RoleMenuRequest(@NotNull(message = "菜单ID列表不能为空") List<Long> menuIds) {
}
