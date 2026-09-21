package com.crazydesert.racing.repository;

import com.crazydesert.racing.ChatBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatBlockRepository extends JpaRepository<ChatBlock, Long> {

    boolean existsByBlockerIdAndBlockedUserId(
            Long blockerId,
            Long blockedUserId
    );

    Optional<ChatBlock> findByBlockerIdAndBlockedUserId(
            Long blockerId,
            Long blockedUserId
    );
}
