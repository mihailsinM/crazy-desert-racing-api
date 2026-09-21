package com.crazydesert.racing.repository;

import com.crazydesert.racing.ChatReadState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatReadStateRepository
        extends JpaRepository<ChatReadState, Long> {

    Optional<ChatReadState> findByConversationIdAndUserId(
            Long conversationId,
            Long userId
    );
}
