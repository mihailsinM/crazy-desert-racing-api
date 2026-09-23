package com.crazydesert.racing;

import com.crazydesert.racing.enums.ChatReportReason;
import com.crazydesert.racing.enums.ChatReportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "chat_reports",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_reporter_message",
                columnNames = {"message_id", "reporter_id"}
        )
)
public class ChatReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private ChatMessage message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ChatReportReason reason;

    @Column(length = 500)
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatReportStatus status = ChatReportStatus.OPEN;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public Long getId() {
        return id;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public User getReporter() {
        return reporter;
    }

    public User getReviewer() {
        return reviewer;
    }

    public ChatReportReason getReason() {
        return reason;
    }

    public String getDetails() {
        return details;
    }

    public ChatReportStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public void setReason(ChatReportReason reason) {
        this.reason = reason;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setStatus(ChatReportStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
