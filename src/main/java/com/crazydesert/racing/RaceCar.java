package com.crazydesert.racing;

import com.crazydesert.racing.dto.ImageFramingProfilesResponse;
import com.crazydesert.racing.dto.ImageFramingResponse;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "race_cars")
public class RaceCar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String brand;
    private int horsePower;
    private String imageUrl;
    private String imagePosition = "CENTER";

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

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "user_id")
    private User owner;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public int getHorsePower() {
        return horsePower;
    }

    public String getImageUrl() {
        if (imageKey != null) {
            return "/media/images/"
                    + imageKey
                    + "?v="
                    + getImageVersion();
        }

        return imageUrl;
    }

    public String getImagePosition() {
        return imagePosition;
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

    public User getOwner() {
        return owner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setHorsePower(int horsePower) {
        this.horsePower = horsePower;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setImagePosition(String imagePosition) {
        this.imagePosition = imagePosition;
    }

    public void setImageFocusX(int imageFocusX) {
        getOrCreateCardImageFraming().setFocusX(imageFocusX);
    }

    public void setImageFocusY(int imageFocusY) {
        getOrCreateCardImageFraming().setFocusY(imageFocusY);
    }

    public void setImageCropPercent(int imageCropPercent) {
        getOrCreateCardImageFraming().setCropPercent(imageCropPercent);
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

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @JsonIgnore
    public boolean hasImage() {
        return imageKey != null
                || (imageUrl != null && !imageUrl.isBlank());
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
