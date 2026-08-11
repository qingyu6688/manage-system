package com.example.managesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.managesystem.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("""
            SELECT DISTINCT m.*
            FROM sys_menu m
            JOIN sys_role_menu rm ON rm.menu_id = m.id
            JOIN sys_user_role ur ON ur.role_id = rm.role_id
            JOIN sys_role r ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND m.enabled = 1
              AND m.deleted = 0
              AND r.enabled = 1
              AND r.deleted = 0
            ORDER BY m.sort, m.id
            """)
    List<SysMenu> selectEnabledByUserId(Long userId);
}
