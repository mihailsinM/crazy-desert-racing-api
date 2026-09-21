package com.crazydesert.racing.controller;

import com.crazydesert.racing.dto.ChatConversationResponse;
import com.crazydesert.racing.dto.ChatImageResponse;
import com.crazydesert.racing.dto.ChatMessageResponse;
import com.crazydesert.racing.dto.ChatReportRequest;
import com.crazydesert.racing.dto.ChatReportResponse;
import com.crazydesert.racing.dto.ChatUnreadResponse;
import com.crazydesert.racing.dto.ChatSupportTopicRequest;
import com.crazydesert.racing.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/conversations")
    public List<ChatConversationResponse> getConversations(
            Authentication authentication) {

        return chatService.getConversations(authentication.getName());
    }

    @PostMapping("/direct/{recipientId}")
    public ChatConversationResponse openDirectConversation(
            Authentication authentication,
            @PathVariable Long recipientId) {

        return chatService.openDirectConversation(
                authentication.getName(),
                recipientId
        );
    }

    @PostMapping("/support")
    public ChatConversationResponse openSupportConversation(
            Authentication authentication) {

        return chatService.openSupportConversation(authentication.getName());
    }

    @PutMapping("/support/topic")
    public ChatConversationResponse updateSupportTopic(
            Authentication authentication,
            @Valid @RequestBody ChatSupportTopicRequest request) {

        return chatService.updateSupportTopic(authentication.getName(), request.topic());
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<ChatMessageResponse> getMessages(
            Authentication authentication,
            @PathVariable Long conversationId,
            @RequestParam(required = false) Long afterId) {

        return chatService.getMessages(
                authentication.getName(),
                conversationId,
                afterId
        );
    }

    @PostMapping(
            value = "/conversations/{conversationId}/messages",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ChatMessageResponse sendMessage(
            Authentication authentication,
            @PathVariable Long conversationId,
            @RequestParam(value = "body", required = false) String body,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        return chatService.sendMessage(
                authentication.getName(),
                conversationId,
                body,
                image
        );
    }

    @PostMapping("/conversations/{conversationId}/read")
    public void markRead(
            Authentication authentication,
            @PathVariable Long conversationId) {

        chatService.markRead(authentication.getName(), conversationId);
    }

    @GetMapping("/unread-count")
    public ChatUnreadResponse getUnreadCount(Authentication authentication) {
        return chatService.getUnreadCount(authentication.getName());
    }

    @PostMapping("/users/{userId}/block")
    public void blockUser(
            Authentication authentication,
            @PathVariable Long userId) {

        chatService.blockUser(authentication.getName(), userId);
    }

    @DeleteMapping("/users/{userId}/block")
    public void unblockUser(
            Authentication authentication,
            @PathVariable Long userId) {

        chatService.unblockUser(authentication.getName(), userId);
    }

    @PostMapping("/messages/{messageId}/reports")
    public ChatReportResponse reportMessage(
            Authentication authentication,
            @PathVariable Long messageId,
            @Valid @RequestBody ChatReportRequest request) {

        return chatService.reportMessage(
                authentication.getName(),
                messageId,
                request
        );
    }

    @GetMapping("/messages/{messageId}/image")
    public ResponseEntity<byte[]> getMessageImage(
            Authentication authentication,
            @PathVariable Long messageId) {

        ChatImageResponse image = chatService.getMessageImage(
                authentication.getName(),
                messageId
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(CacheControl.noStore())
                .body(image.data());
    }
}
