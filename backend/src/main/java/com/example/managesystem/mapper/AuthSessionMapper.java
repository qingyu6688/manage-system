package com.example.managesystem.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface AuthSessionMapper {

    @Insert("INSERT INTO sys_auth_session(id,user_id,refresh_hash,expires_at) VALUES(#{id},#{userId},#{hash},#{expiry})")
    int create(String id, Long userId, String hash, LocalDateTime expiry);

    @Select("SELECT COUNT(*) FROM sys_auth_session WHERE id=#{id} AND user_id=#{userId} AND expires_at > CURRENT_TIMESTAMP")
    int active(String id, Long userId);

    @Update("""
            UPDATE sys_auth_session SET refresh_hash=#{nextHash}, updated_at=CURRENT_TIMESTAMP
            WHERE id=#{id} AND user_id=#{userId} AND refresh_hash=#{previousHash}
              AND expires_at > CURRENT_TIMESTAMP
            """)
    int rotate(String id, Long userId, String previousHash, String nextHash);

    @Delete("DELETE FROM sys_auth_session WHERE id=#{id} AND user_id=#{userId}")
    int revoke(String id, Long userId);

    @Delete("DELETE FROM sys_auth_session WHERE expires_at <= CURRENT_TIMESTAMP")
    int purgeExpired();
}
