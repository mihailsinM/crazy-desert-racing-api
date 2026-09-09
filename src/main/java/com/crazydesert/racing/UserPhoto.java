package com.crazydesert.racing;

import com.crazydesert.racing.dto.ImageFramingProfilesResponse;
import com.crazydesert.racing.dto.ImageFramingResponse;
import com.crazydesert.racing.enums.UserPhotoVisibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_photos")
public class UserPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "image_key", nullable = false, unique = true, length = 36)
    private String imageKey;

    @Column(name = "image_version", nullable = false)
    private long imageVersion;

    @Column(length = 300)
    private String caption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserPhotoVisibility visibility = UserPhotoVisibility.MEMBERS_ONLY;

    @Column(name = "rights_confirmed", nullable = false)
    private boolean rightsConfirmed;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "focusX",
                    column = @Column(name = "avatar_focus_x")
            ),
            @AttributeOverride(
                    name = "focusY",
                    column = @Column(name = "avatar_focus_y")
            ),
            @AttributeOverride(
                    name = "cropPercent",
                    column = @Column(name = "avatar_crop_percent")
            )
    })
    private ImageFraming avatarImageFraming = new ImageFraming();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "focusX",
                    column = @Column(name = "card_focus_x")
            ),
            @AttributeOverride(
                    name = "focusY",
                    column = @Column(name = "card_focus_y")
            ),
            @AttributeOverride(
                    name = "cropPercent",
                    column = @Column(name = "card_crop_percent")
            )
    })
    private ImageFraming cardImageFraming = new ImageFraming();

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public String getImageKey() {
        return imageKey;
    }

    public long getImageVersion() {
        return imageVersion;
    }

    public String getCaption() {
        return caption;
    }

    public UserPhotoVisibility getVisibility() {
        return visibility == null
                ? UserPhotoVisibility.MEMBERS_ONLY
                : visibility;
    }

    public boolean isRightsConfirmed() {
        return rightsConfirmed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ImageFramingProfilesResponse getImageFraming() {
        ImageFraming avatar = getOrCreateAvatarImageFraming();
        ImageFraming card = getEffectiveCardImageFraming(avatar);

        return new ImageFramingProfilesResponse(
                ImageFramingResponse.from(avatar),
                ImageFramingResponse.from(card)
        );
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public void setImageVersion(long imageVersion) {
        this.imageVersion = imageVersion;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setVisibility(UserPhotoVisibility visibility) {
        this.visibility = visibility;
    }

    public void setRightsConfirmed(boolean rightsConfirmed) {
        this.rightsConfirmed = rightsConfirmed;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public void applyCardImageFraming(
            int focusX,
            int focusY,
            int cropPercent) {

        ImageFraming framing = getOrCreateCardImageFraming();
        framing.setFocusX(focusX);
        framing.setFocusY(focusY);
        framing.setCropPercent(cropPercent);
    }

    private ImageFraming getOrCreateAvatarImageFraming() {
        if (avatarImageFraming == null) {
            avatarImageFraming = new ImageFraming();
        }

        return avatarImageFraming;
    }

    private ImageFraming getOrCreateCardImageFraming() {
        if (cardImageFraming == null || cardImageFraming.isUnset()) {
            cardImageFraming = new ImageFraming();
        }

        return cardImageFraming;
    }

    private ImageFraming getEffectiveCardImageFraming(
            ImageFraming avatarImageFraming) {

        if (cardImageFraming == null || cardImageFraming.isUnset()) {
            return avatarImageFraming;
        }

        return cardImageFraming;
    }
}
