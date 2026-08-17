package com.crazydesert.racing;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "race_cars")
public class RaceCar {

    private static final int DEFAULT_IMAGE_FOCUS = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String brand;
    private int horsePower;
    private String imageUrl;
    private String imagePosition = "CENTER";

    @Column(name = "image_focus_x")
    private Integer imageFocusX = DEFAULT_IMAGE_FOCUS;

    @Column(name = "image_focus_y")
    private Integer imageFocusY = DEFAULT_IMAGE_FOCUS;

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
        return imageUrl;
    }

    public String getImagePosition() {
        return imagePosition;
    }

    public int getImageFocusX() {
        return imageFocusX == null
                ? DEFAULT_IMAGE_FOCUS
                : imageFocusX;
    }

    public int getImageFocusY() {
        return imageFocusY == null
                ? DEFAULT_IMAGE_FOCUS
                : imageFocusY;
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
        this.imageFocusX = imageFocusX;
    }

    public void setImageFocusY(int imageFocusY) {
        this.imageFocusY = imageFocusY;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}