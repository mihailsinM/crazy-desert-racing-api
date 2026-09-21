package com.crazydesert.racing.repository;

import com.crazydesert.racing.ChatConversation;
import com.crazydesert.racing.enums.ChatConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository
        extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation> findByConversationKey(String conversationKey);

    @Query("""
            select conversation
            from ChatConversation conversation
            where conversation.type = com.crazydesert.racing.enums.ChatConversationType.DIRECT
              and (conversation.userOne.id = :userId
                   or conversation.userTwo.id = :userId)
            order by conversation.updatedAt desc
            """)
    List<ChatConversation> findDirectConversationsForUser(
            @Param("userId") Long userId
    );

    Optional<ChatConversation> findByTypeAndSupportOwnerId(
            ChatConversationType type,
            Long supportOwnerId
    );

    List<ChatConversation> findByTypeOrderByUpdatedAtDesc(
            ChatConversationType type
    );
}
