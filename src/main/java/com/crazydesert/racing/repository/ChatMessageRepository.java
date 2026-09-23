package com.crazydesert.racing.repository;

import com.crazydesert.racing.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop100ByConversationIdOrderByIdDesc(
            Long conversationId
    );

    List<ChatMessage> findTop100ByConversationIdAndIdGreaterThanOrderByIdAsc(
            Long conversationId,
            Long afterId
    );

    Optional<ChatMessage> findFirstByConversationIdOrderByIdDesc(
            Long conversationId
    );

    long countByConversationIdAndSenderIdNot(
            Long conversationId,
            Long senderId
    );

    long countByConversationIdAndCreatedAtAfterAndSenderIdNot(
            Long conversationId,
            LocalDateTime createdAt,
            Long senderId
    );

    long countBySenderIdAndCreatedAtAfter(
            Long senderId,
            LocalDateTime createdAt
    );
}
