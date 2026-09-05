package com.crazydesert.racing;

import com.crazydesert.racing.dto.ImageFramingProfilesResponse;
import com.crazydesert.racing.dto.ImageFramingResponse;
import com.crazydesert.racing.enums.RaceStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "races")
public class Race {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private LocalDate startDate;
    private int maxParticipants;

    @Enumerated(EnumType.STRING)
    private RaceStatus status;

    @Column(length = 1000)
    private String adminMessage;

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

    @Column(name = "image_key", unique = true, length = 36)
    private String imageKey;

    @Column(name = "image_version")
    private Long imageVersion;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public RaceStatus getStatus() {
        return status;
    }

    public String getAdminMessage() {
        return adminMessage;
    }

    public String getImageUrl() {
        if (imageKey == null) {
            return null;
        }

        return "/media/images/"
                + imageKey
                + "?v="
                + getImageVersion();
    }

    public ImageFramingProfilesResponse getImageFraming() {
        ImageFraming card = getOrCreateCardImageFraming();
        ImageFraming avatar = getEffectiveAvatarImageFraming(card);

        return new ImageFramingProfilesResponse(
                ImageFramingResponse.from(avatar),
                ImageFramingResponse.from(card)
        );
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

    @JsonIgnore
    public String getImageKey() {
        return imageKey;
    }

    @JsonIgnore
    public long getImageVersion() {
        return imageVersion == null ? 0L : imageVersion;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public void setStatus(RaceStatus status) {
        this.status = status;
    }

    public void setAdminMessage(String adminMessage) {
        this.adminMessage = adminMessage;
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

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public void setImageVersion(long imageVersion) {
        this.imageVersion = imageVersion;
    }

    @JsonIgnore
    public boolean hasImage() {
        return imageKey != null;
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
