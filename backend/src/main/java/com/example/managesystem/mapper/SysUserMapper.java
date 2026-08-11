package com.example.managesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.managesystem.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("""
            SELECT r.code
            FROM sys_role r
            JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId} AND r.enabled = 1 AND r.deleted = 0
            ORDER BY r.id
            """)
    List<String> selectRoleCodesByUserId(Long userId);

    @Select("""
            SELECT DISTINCT m.permission
            FROM sys_menu m
            JOIN sys_role_menu rm ON rm.menu_id = m.id
            JOIN sys_user_role ur ON ur.role_id = rm.role_id
            JOIN sys_role r ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND m.menu_type = 'BUTTON'
              AND m.permission IS NOT NULL
              AND m.permission <> ''
              AND m.enabled = 1
              AND m.deleted = 0
              AND r.enabled = 1
              AND r.deleted = 0
            ORDER BY m.permission
            """)
    List<String> selectPermissionsByUserId(Long userId);
}
