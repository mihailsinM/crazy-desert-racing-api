package com.crazydesert.racing;

import com.crazydesert.racing.enums.UserPhotoReportReason;
import com.crazydesert.racing.enums.UserPhotoReportStatus;
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
        name = "user_photo_reports",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_photo_reporter",
                columnNames = {"photo_id", "reporter_id"}
        )
)
public class UserPhotoReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "photo_id", nullable = false)
    private UserPhoto photo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserPhotoReportReason reason;

    @Column(length = 500)
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserPhotoReportStatus status = UserPhotoReportStatus.OPEN;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public Long getId() {
        return id;
    }

    public UserPhoto getPhoto() {
        return photo;
    }

    public User getReporter() {
        return reporter;
    }

    public UserPhotoReportReason getReason() {
        return reason;
    }

    public String getDetails() {
        return details;
    }

    public UserPhotoReportStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setPhoto(UserPhoto photo) {
        this.photo = photo;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public void setReason(UserPhotoReportReason reason) {
        this.reason = reason;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setStatus(UserPhotoReportStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
