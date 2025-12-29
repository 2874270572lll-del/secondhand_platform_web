package com.lll_cxt.marketplace.secondhand.controller;

import com.lll_cxt.marketplace.secondhand.dto.ConversationDTO;
import com.lll_cxt.marketplace.secondhand.dto.MessageDTO;
import com.lll_cxt.marketplace.secondhand.dto.Result;
import com.lll_cxt.marketplace.secondhand.entity.Message;
import com.lll_cxt.marketplace.secondhand.entity.Product;
import com.lll_cxt.marketplace.secondhand.entity.User;
import com.lll_cxt.marketplace.secondhand.repository.MessageRepository;
import com.lll_cxt.marketplace.secondhand.repository.ProductRepository;
import com.lll_cxt.marketplace.secondhand.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageController(MessageRepository messageRepository,
                             UserRepository userRepository,
                             ProductRepository productRepository,
                             SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 发送消息（WebSocket）
     */
    @MessageMapping("/chat")
    public void sendMessage(@Payload MessageDTO messageDTO) {
        // 保存消息到数据库
        Message message = new Message();
        message.setSenderId(messageDTO.getSenderId());
        message.setReceiverId(messageDTO.getReceiverId());
        message.setProductId(messageDTO.getProductId());
        message.setContent(messageDTO.getContent());
        message.setIsRead(false);

        Message savedMessage = messageRepository.save(message);

        // 转换为DTO
        MessageDTO dto = convertToDTO(savedMessage);

        // 发送给接收者
        messagingTemplate.convertAndSendToUser(
                messageDTO.getReceiverId().toString(),
                "/queue/messages",
                dto
        );

        // 发送给发送者（确认）
        messagingTemplate.convertAndSendToUser(
                messageDTO.getSenderId().toString(),
                "/queue/messages",
                dto
        );
    }

    /**
     * 发送消息（HTTP接口）
     * 【修复】将 @RequestHeader 改为 @RequestAttribute
     */
    @PostMapping("/send")
    public Result<MessageDTO> sendMessageHttp(
            @RequestAttribute("userId") Long userId,
            @RequestBody MessageDTO messageDTO) {
        // 设置发送者ID
        messageDTO.setSenderId(userId);

        // 保存消息到数据库
        Message message = new Message();
        message.setSenderId(messageDTO.getSenderId());
        message.setReceiverId(messageDTO.getReceiverId());
        message.setProductId(messageDTO.getProductId());
        message.setContent(messageDTO.getContent());
        message.setIsRead(false);

        Message savedMessage = messageRepository.save(message);

        // 转换为DTO并返回
        MessageDTO dto = convertToDTO(savedMessage);
        return Result.success("发送成功", dto);
    }

    /**
     * 获取聊天历史
     * 【修复】将 @RequestHeader 改为 @RequestAttribute
     */
    @GetMapping("/history/{partnerId}")
    public Result<List<MessageDTO>> getChatHistory(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long partnerId) {
        List<Message> messages = messageRepository.findChatHistory(userId, partnerId);
        List<MessageDTO> dtos = messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 标记为已读
        messages.stream()
                .filter(m -> m.getReceiverId().equals(userId) && !m.getIsRead())
                .forEach(m -> {
                    m.setIsRead(true);
                    messageRepository.save(m);
                });

        return Result.success("获取成功", dtos);
    }

    /**
     * 获取聊天会话列表
     * 【修复】将 @RequestHeader 改为 @RequestAttribute
     */
//    @GetMapping("/conversations")
//    public Result<List<User>> getConversations(@RequestAttribute("userId") Long userId) {
//        List<Long> partnerIds = messageRepository.findChatPartners(userId);
//        List<User> partners = userRepository.findAllById(partnerIds);
//        return Result.success("获取成功", partners);
//    }

    /**
     * 获取未读消息数量
     * 【修复】将 @RequestHeader 改为 @RequestAttribute
     */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@RequestAttribute("userId") Long userId) {
        Long count = messageRepository.countByReceiverIdAndIsReadFalse(userId);
        return Result.success("获取成功", count);
    }

    /**
     * 标记消息为已读
     * 【修复】将 @RequestHeader 改为 @RequestAttribute
     */
    @PutMapping("/mark-read/{partnerId}")
    public Result<Void> markAsRead(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long partnerId) {
        List<Message> messages = messageRepository.findChatHistory(userId, partnerId);
        messages.stream()
                .filter(m -> m.getReceiverId().equals(userId) && !m.getIsRead())
                .forEach(m -> {
                    m.setIsRead(true);
                    messageRepository.save(m);
                });
        return Result.success("标记成功", null);
    }

    /**
     * 转换为DTO
     */
    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        BeanUtils.copyProperties(message, dto);

        // 设置发送者信息
        User sender = userRepository.findById(message.getSenderId()).orElse(null);
        if (sender != null) {
            dto.setSenderName(sender.getNickname());
            dto.setSenderAvatar(sender.getAvatar());
        }

        // 设置接收者信息
        User receiver = userRepository.findById(message.getReceiverId()).orElse(null);
        if (receiver != null) {
            dto.setReceiverName(receiver.getNickname());
            dto.setReceiverAvatar(receiver.getAvatar());
        }

        // 设置商品信息
        if (message.getProductId() != null) {
            Product product = productRepository.findById(message.getProductId()).orElse(null);
            if (product != null) {
                dto.setProductTitle(product.getTitle());
            }
        }

        return dto;
    }
    @GetMapping("/conversations")
    public Result<List<ConversationDTO>> getConversations(@RequestAttribute("userId") Long userId) {
        List<Long> partnerIds = messageRepository.findChatPartners(userId);
        List<User> partners = userRepository.findAllById(partnerIds);

        // 转换为DTO并添加未读消息数
        List<ConversationDTO> conversations = partners.stream().map(partner -> {
            ConversationDTO dto = new ConversationDTO();
            dto.setId(partner.getId());
            dto.setUsername(partner.getUsername());
            dto.setNickname(partner.getNickname());
            dto.setAvatar(partner.getAvatar());

            // 计算该会话的未读消息数
            Long unreadCount = messageRepository.countBySenderIdAndReceiverIdAndIsReadFalse(
                    partner.getId(),
                    userId
            );
            dto.setUnreadCount(unreadCount);

            return dto;
        }).collect(Collectors.toList());

        return Result.success("获取成功", conversations);
    }

}