package com.example.managesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.managesystem.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM sys_role WHERE code=#{code} AND (#{id} IS NULL OR id != #{id})")
    long countReservedCode(String code, Long id);

    @Select("""
            SELECT r.*
            FROM sys_role r
            JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId} AND r.deleted = 0
            ORDER BY r.id
            """)
    List<SysRole> selectByUserId(Long userId);
}
