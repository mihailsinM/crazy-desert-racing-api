package com.crazydesert.racing.repository;

import com.crazydesert.racing.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
            select count(message.id)
            from ChatMessage message
            join message.conversation conversation
            left join ChatReadState readState
                on readState.conversation = conversation
               and readState.user.id = :userId
            where message.sender.id <> :userId
              and (readState.lastReadAt is null
                   or message.createdAt > readState.lastReadAt)
              and ((conversation.type = com.crazydesert.racing.enums.ChatConversationType.DIRECT
                    and (conversation.userOne.id = :userId
                         or conversation.userTwo.id = :userId))
                   or (conversation.type = com.crazydesert.racing.enums.ChatConversationType.SUPPORT
                       and (:isAdmin = true or conversation.supportOwner.id = :userId)))
            """)
    long countUnreadAccessible(
            @Param("userId") Long userId,
            @Param("isAdmin") boolean isAdmin
    );

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
