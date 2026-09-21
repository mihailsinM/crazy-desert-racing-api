package com.crazydesert.racing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_blocks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_block_pair",
                columnNames = {"blocker_id", "blocked_user_id"}
        )
)
public class ChatBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocked_user_id", nullable = false)
    private User blockedUser;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public User getBlocker() {
        return blocker;
    }

    public User getBlockedUser() {
        return blockedUser;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setBlocker(User blocker) {
        this.blocker = blocker;
    }

    public void setBlockedUser(User blockedUser) {
        this.blockedUser = blockedUser;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
