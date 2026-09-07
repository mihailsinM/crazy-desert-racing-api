package com.crazydesert.racing;

import com.crazydesert.racing.enums.DesertLiveCategory;
import com.crazydesert.racing.enums.DesertLiveModerationStatus;
import com.crazydesert.racing.enums.DesertLiveSource;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(
        name = "desert_live_items",
        indexes = {
                @Index(
                        name = "idx_desert_live_public_feed",
                        columnList = "moderation_status, category, active_from, active_until"
                ),
                @Index(
                        name = "idx_desert_live_author",
                        columnList = "created_by_user_id, created_at"
                ),
                @Index(
                        name = "idx_desert_live_linked_race",
                        columnList = "linked_race_id"
                )
        }
)
public class DesertLiveItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DesertLiveCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DesertLiveSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false, length = 20)
    private DesertLiveModerationStatus moderationStatus;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "target_url", length = 500)
    private String targetUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_race_id", unique = true)
    private Race linkedRace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User createdBy;

    @Column(name = "moderated_by_user_id")
    private Long moderatedByUserId;

    @Column(name = "moderation_note", length = 500)
    private String moderationNote;

    @Column(name = "display_priority", nullable = false)
    private int displayPriority;

    @Column(name = "active_from")
    private Instant activeFrom;

    @Column(name = "active_until")
    private Instant activeUntil;

    @Column(name = "image_key", unique = true, length = 36)
    private String imageKey;

    @Column(name = "image_version")
    private Long imageVersion;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "focusX",
                    column = @Column(name = "image_focus_x")
            ),
            @AttributeOverride(
                    name = "focusY",
                    column = @Column(name = "image_focus_y")
            ),
            @AttributeOverride(
                    name = "cropPercent",
                    column = @Column(name = "image_crop_percent")
            )
    })
    private ImageFraming cardImageFraming = new ImageFraming();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "focusX",
                    column = @Column(name = "avatar_image_focus_x")
            ),
            @AttributeOverride(
                    name = "focusY",
                    column = @Column(name = "avatar_image_focus_y")
            ),
            @AttributeOverride(
                    name = "cropPercent",
                    column = @Column(name = "avatar_image_crop_percent")
            )
    })
    private ImageFraming avatarImageFraming = new ImageFraming();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "moderated_at")
    private Instant moderatedAt;

    @Version
    private long version;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public DesertLiveCategory getCategory() {
        return category;
    }

    public DesertLiveSource getSource() {
        return source;
    }

    public DesertLiveModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public Race getLinkedRace() {
        return linkedRace;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public Long getModeratedByUserId() {
        return moderatedByUserId;
    }

    public String getModerationNote() {
        return moderationNote;
    }

    public int getDisplayPriority() {
        return displayPriority;
    }

    public Instant getActiveFrom() {
        return activeFrom;
    }

    public Instant getActiveUntil() {
        return activeUntil;
    }

    public String getImageKey() {
        return imageKey;
    }

    public long getImageVersion() {
        return imageVersion == null ? 0L : imageVersion;
    }

    public int getImageFocusX() {
        return getOrCreateCardImageFraming().getFocusX();
    }

    public int getImageFocusY() {
        return getOrCreateCardImageFraming().getFocusY();
    }

    public int getImageCropPercent() {
        return getOrCreateCardImageFraming().getCropPercent();
    }

    public ImageFraming getCardImageFraming() {
        return getOrCreateCardImageFraming();
    }

    public ImageFraming getAvatarImageFraming() {
        return getEffectiveAvatarImageFraming(
                getOrCreateCardImageFraming()
        );
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getModeratedAt() {
        return moderatedAt;
    }

    public void setCategory(DesertLiveCategory category) {
        this.category = category;
    }

    public void setSource(DesertLiveSource source) {
        this.source = source;
    }

    public void setModerationStatus(DesertLiveModerationStatus moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public void setLinkedRace(Race linkedRace) {
        this.linkedRace = linkedRace;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public void setModeratedByUserId(Long moderatedByUserId) {
        this.moderatedByUserId = moderatedByUserId;
    }

    public void setModerationNote(String moderationNote) {
        this.moderationNote = moderationNote;
    }

    public void setDisplayPriority(int displayPriority) {
        this.displayPriority = displayPriority;
    }

    public void setActiveFrom(Instant activeFrom) {
        this.activeFrom = activeFrom;
    }

    public void setActiveUntil(Instant activeUntil) {
        this.activeUntil = activeUntil;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public void setImageVersion(long imageVersion) {
        this.imageVersion = imageVersion;
    }

    public void setImageFocusX(int imageFocusX) {
        getOrCreateCardImageFraming().setFocusX(imageFocusX);
    }

    public void setImageFocusY(int imageFocusY) {
        getOrCreateCardImageFraming().setFocusY(imageFocusY);
    }

    public void applyCardImageFraming(
            int focusX,
            int focusY,
            int cropPercent) {

        ImageFraming framing = getOrCreateCardImageFraming();
        framing.setFocusX(focusX);
        framing.setFocusY(focusY);
        framing.setCropPercent(cropPercent);
    }

    public void applyAvatarImageFraming(
            int focusX,
            int focusY,
            int cropPercent) {

        ImageFraming framing = getOrCreateAvatarImageFraming();
        framing.setFocusX(focusX);
        framing.setFocusY(focusY);
        framing.setCropPercent(cropPercent);
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setModeratedAt(Instant moderatedAt) {
        this.moderatedAt = moderatedAt;
    }

    private ImageFraming getOrCreateCardImageFraming() {
        if (cardImageFraming == null) {
            cardImageFraming = new ImageFraming();
        }

        return cardImageFraming;
    }

    private ImageFraming getOrCreateAvatarImageFraming() {
        if (avatarImageFraming == null || avatarImageFraming.isUnset()) {
            avatarImageFraming = new ImageFraming();
        }

        return avatarImageFraming;
    }

    private ImageFraming getEffectiveAvatarImageFraming(
            ImageFraming cardImageFraming) {

        if (avatarImageFraming == null || avatarImageFraming.isUnset()) {
            return cardImageFraming;
        }

        return avatarImageFraming;
    }
}
