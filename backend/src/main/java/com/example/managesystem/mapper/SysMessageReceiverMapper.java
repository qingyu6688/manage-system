package com.example.managesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.managesystem.entity.SysMessageReceiver;
import com.example.managesystem.vo.MessageListItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysMessageReceiverMapper extends BaseMapper<SysMessageReceiver> {

    @Select("""
            <script>
            SELECT mr.id AS receiverId,
                   m.id AS messageId,
                   m.title,
                   m.content,
                   m.type,
                   m.sender,
                   m.created_at AS createdAt,
                   CASE WHEN mr.read_at IS NULL THEN 0 ELSE 1 END AS `read`,
                   mr.read_at AS readAt
            FROM sys_message_receiver mr
            JOIN sys_message m ON m.id = mr.message_id
            WHERE mr.user_id = #{userId}
              AND mr.deleted = 0
              AND m.deleted = 0
            <if test='keyword != null and keyword != ""'>
              AND m.title LIKE CONCAT('%', #{keyword}, '%')
            </if>
            <if test='type != null and type != ""'>
              AND m.type = #{type}
            </if>
            <if test='readStatus == "READ"'>
              AND mr.read_at IS NOT NULL
            </if>
            <if test='readStatus == "UNREAD"'>
              AND mr.read_at IS NULL
            </if>
            ORDER BY CASE WHEN mr.read_at IS NULL THEN 0 ELSE 1 END, m.created_at DESC
            </script>
            """)
    IPage<MessageListItem> selectMessagePage(
            Page<MessageListItem> page,
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("type") String type,
            @Param("readStatus") String readStatus
    );

    @Select("""
            SELECT COUNT(*)
            FROM sys_message_receiver mr
            JOIN sys_message m ON m.id = mr.message_id
            WHERE mr.user_id = #{userId}
              AND mr.read_at IS NULL
              AND mr.deleted = 0
              AND m.deleted = 0
            """)
    long selectUnreadCount(Long userId);

    @Select("""
            SELECT *
            FROM sys_message_receiver
            WHERE id = #{receiverId} AND user_id = #{userId} AND deleted = 0
            """)
    SysMessageReceiver selectOwnedReceiver(
            @Param("receiverId") Long receiverId,
            @Param("userId") Long userId
    );

    @Update("""
            UPDATE sys_message_receiver
            SET read_at = CURRENT_TIMESTAMP
            WHERE user_id = #{userId} AND read_at IS NULL AND deleted = 0
            """)
    int markAllRead(Long userId);
}
