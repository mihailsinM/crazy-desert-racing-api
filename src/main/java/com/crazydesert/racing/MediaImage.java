package com.crazydesert.racing;

import com.crazydesert.racing.enums.MediaImageVisibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "media_images")
public class MediaImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_key", nullable = false, unique = true, length = 36)
    private String imageKey;

    @JsonIgnore
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] data;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "image_version", nullable = false)
    private long imageVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaImageVisibility visibility = MediaImageVisibility.PRIVATE;

    public Long getId() {
        return id;
    }

    public String getImageKey() {
        return imageKey;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }

    public long getImageVersion() {
        return imageVersion;
    }

    public MediaImageVisibility getVisibility() {
        return visibility == null
                ? MediaImageVisibility.PRIVATE
                : visibility;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setImageVersion(long imageVersion) {
        this.imageVersion = imageVersion;
    }

    public void setVisibility(MediaImageVisibility visibility) {
        this.visibility = visibility;
    }
}
