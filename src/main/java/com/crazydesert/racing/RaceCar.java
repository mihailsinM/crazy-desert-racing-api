package com.crazydesert.racing;

import com.crazydesert.racing.dto.ImageFramingProfilesResponse;
import com.crazydesert.racing.dto.ImageFramingResponse;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "gallery_photo_id")
    private UserPhoto galleryPhoto;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "race_car_gallery_photos",
            joinColumns = @JoinColumn(name = "race_car_id"),
            inverseJoinColumns = @JoinColumn(name = "user_photo_id")
    )
    @OrderBy("createdAt DESC")
    @JsonIgnore
    private Set<UserPhoto> galleryPhotos = new LinkedHashSet<>();

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
        if (galleryPhoto != null) {
            return "/driver-photos/" + galleryPhoto.getId() + "/image";
        }

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
        if (galleryPhoto != null) {
            return galleryPhoto.getImageFraming();
        }

        ImageFraming card = getOrCreateCardImageFraming();
        ImageFraming avatar = getEffectiveAvatarImageFraming(card);

        return new ImageFramingProfilesResponse(
                ImageFramingResponse.from(avatar),
                ImageFramingResponse.from(card)
        );
    }

    public int getImageFocusX() {
        return getImageFraming().card().focusX();
    }

    public int getImageFocusY() {
        return getImageFraming().card().focusY();
    }

    public int getImageCropPercent() {
        return getImageFraming().card().cropPercent();
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

    public UserPhoto getGalleryPhoto() {
        return galleryPhoto;
    }

    public void setGalleryPhoto(UserPhoto galleryPhoto) {
        this.galleryPhoto = galleryPhoto;
        if (galleryPhoto != null) {
            getGalleryPhotos().add(galleryPhoto);
        }
    }

    @JsonIgnore
    public Set<UserPhoto> getGalleryPhotos() {
        if (galleryPhotos == null) {
            galleryPhotos = new LinkedHashSet<>();
        }
        if (galleryPhoto != null) {
            galleryPhotos.add(galleryPhoto);
        }

        return galleryPhotos;
    }

    public List<Long> getGalleryPhotoIds() {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        if (galleryPhoto != null) {
            ids.add(galleryPhoto.getId());
        }
        getGalleryPhotos().stream()
                .map(UserPhoto::getId)
                .forEach(ids::add);
        return List.copyOf(ids);
    }

    public void removeGalleryPhoto(UserPhoto photo) {
        boolean removingCover = galleryPhoto != null
                && galleryPhoto.getId().equals(photo.getId());
        if (removingCover) {
            galleryPhoto = null;
        }

        getGalleryPhotos().removeIf(item ->
                item.getId() != null && item.getId().equals(photo.getId())
        );

        if (removingCover) {
            galleryPhoto = getGalleryPhotos().stream().findFirst().orElse(null);
        }
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @JsonIgnore
    public boolean hasImage() {
        return imageKey != null
                || galleryPhoto != null
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
