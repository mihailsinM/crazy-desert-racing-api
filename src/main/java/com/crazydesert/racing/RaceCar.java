package com.crazydesert.racing;

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
    private ImageFraming imageFraming = new ImageFraming();

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

    public int getImageFocusX() {
        return getOrCreateImageFraming().getFocusX();
    }

    public int getImageFocusY() {
        return getOrCreateImageFraming().getFocusY();
    }

    public int getImageCropPercent() {
        return getOrCreateImageFraming().getCropPercent();
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
        getOrCreateImageFraming().setFocusX(imageFocusX);
    }

    public void setImageFocusY(int imageFocusY) {
        getOrCreateImageFraming().setFocusY(imageFocusY);
    }

    public void setImageCropPercent(int imageCropPercent) {
        getOrCreateImageFraming().setCropPercent(imageCropPercent);
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

    private ImageFraming getOrCreateImageFraming() {
        if (imageFraming == null) {
            imageFraming = new ImageFraming();
        }

        return imageFraming;
    }
}
