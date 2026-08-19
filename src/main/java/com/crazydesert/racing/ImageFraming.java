package com.crazydesert.racing;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ImageFraming {

    public static final int DEFAULT_FOCUS = 50;
    public static final int DEFAULT_CROP_PERCENT = 0;

    @Column(name = "image_focus_x")
    private Integer focusX = DEFAULT_FOCUS;

    @Column(name = "image_focus_y")
    private Integer focusY = DEFAULT_FOCUS;

    @Column(name = "image_crop_percent")
    private Integer cropPercent = DEFAULT_CROP_PERCENT;

    public int getFocusX() {
        return focusX == null ? DEFAULT_FOCUS : focusX;
    }

    public int getFocusY() {
        return focusY == null ? DEFAULT_FOCUS : focusY;
    }

    public int getCropPercent() {
        return cropPercent == null
                ? DEFAULT_CROP_PERCENT
                : cropPercent;
    }

    public void setFocusX(int focusX) {
        this.focusX = focusX;
    }

    public void setFocusY(int focusY) {
        this.focusY = focusY;
    }

    public void setCropPercent(int cropPercent) {
        this.cropPercent = cropPercent;
    }
}
