package com.crazydesert.racing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @JsonIgnore
    @Column(name = "body_ciphertext", columnDefinition = "text")
    private String bodyCiphertext;

    @JsonIgnore
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_ciphertext", columnDefinition = "bytea")
    private byte[] imageCiphertext;

    @Column(name = "image_content_type", length = 50)
    private String imageContentType;

    @Column(name = "image_original_name", length = 180)
    private String imageOriginalName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public ChatConversation getConversation() {
        return conversation;
    }

    public User getSender() {
        return sender;
    }

    public String getBodyCiphertext() {
        return bodyCiphertext;
    }

    public byte[] getImageCiphertext() {
        return imageCiphertext;
    }

    public String getImageContentType() {
        return imageContentType;
    }

    public String getImageOriginalName() {
        return imageOriginalName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean hasImage() {
        return imageCiphertext != null && imageCiphertext.length > 0;
    }

    public void setConversation(ChatConversation conversation) {
        this.conversation = conversation;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setBodyCiphertext(String bodyCiphertext) {
        this.bodyCiphertext = bodyCiphertext;
    }

    public void setImageCiphertext(byte[] imageCiphertext) {
        this.imageCiphertext = imageCiphertext;
    }

    public void setImageContentType(String imageContentType) {
        this.imageContentType = imageContentType;
    }

    public void setImageOriginalName(String imageOriginalName) {
        this.imageOriginalName = imageOriginalName;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
