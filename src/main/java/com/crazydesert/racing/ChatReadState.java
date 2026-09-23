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
        name = "chat_read_states",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_read_state_user",
                columnNames = {"conversation_id", "user_id"}
        )
)
public class ChatReadState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    public Long getId() {
        return id;
    }

    public ChatConversation getConversation() {
        return conversation;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getLastReadAt() {
        return lastReadAt;
    }

    public void setConversation(ChatConversation conversation) {
        this.conversation = conversation;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setLastReadAt(LocalDateTime lastReadAt) {
        this.lastReadAt = lastReadAt;
    }
}
