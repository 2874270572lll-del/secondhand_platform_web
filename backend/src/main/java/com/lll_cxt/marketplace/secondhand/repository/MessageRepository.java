package com.lll_cxt.marketplace.secondhand.repository;

import com.lll_cxt.marketplace.secondhand.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 消息数据访问接口
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 获取两个用户之间的聊天记录
     */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.senderId = ?1 AND m.receiverId = ?2) OR " +
            "(m.senderId = ?2 AND m.receiverId = ?1) " +
            "ORDER BY m.createdAt ASC")
    List<Message> findChatHistory(Long userId1, Long userId2);

    /**
     * 获取用户的所有聊天会话（去重对方ID）
     */
    @Query("SELECT DISTINCT CASE WHEN m.senderId = ?1 THEN m.receiverId ELSE m.senderId END " +
            "FROM Message m WHERE m.senderId = ?1 OR m.receiverId = ?1")
    List<Long> findChatPartners(Long userId);

    /**
     * 获取未读消息数量
     */
    Long countByReceiverIdAndIsReadFalse(Long receiverId);
    Long countBySenderIdAndReceiverIdAndIsReadFalse(Long senderId, Long receiverId);
}
