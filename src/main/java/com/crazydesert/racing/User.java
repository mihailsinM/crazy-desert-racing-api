package com.crazydesert.racing;

import com.crazydesert.racing.dto.ImageFramingProfilesResponse;
import com.crazydesert.racing.dto.ImageFramingResponse;
import com.crazydesert.racing.enums.MembershipTier;
import com.crazydesert.racing.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int age;
    @Column(nullable = false, unique = true)
    private String email;

    private String licenseCategory;
    private boolean licenseVerified;
    @JsonIgnore
    private String password;

    @JsonIgnore
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "avatar_data", columnDefinition = "bytea")
    private byte[] avatarData;

    @JsonIgnore
    @Column(name = "avatar_content_type", length = 50)
    private String avatarContentType;

    @JsonIgnore
    private Long avatarVersion;

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
                    column = @Column(name = "profile_card_focus_x")
            ),
            @AttributeOverride(
                    name = "focusY",
                    column = @Column(name = "profile_card_focus_y")
            ),
            @AttributeOverride(
                    name = "cropPercent",
                    column = @Column(name = "profile_card_crop_percent")
            )
    })
    private ImageFraming cardImageFraming = new ImageFraming();

    @Column(name = "profile_bio", length = 500)
    private String profileBio;

    @Column(name = "profile_location", length = 120)
    private String profileLocation;

    @Column(name = "show_cars")
    private Boolean showCars = false;

    @Column(name = "show_race_history")
    private Boolean showRaceHistory = false;

    @Column(name = "show_photos")
    private Boolean showPhotos = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_tier", length = 20)
    private MembershipTier membershipTier = MembershipTier.STANDARD;

    @Column(name = "membership_expires_at")
    private LocalDateTime membershipExpiresAt;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_photo_id")
    private UserPhoto profilePhoto;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "owner")
    @JsonManagedReference
    private List<RaceCar> raceCars;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public String getLicenseCategory() {
        return licenseCategory;
    }

    public boolean isLicenseVerified() {
        return licenseVerified;
    }

    public String getPassword() {
        return password;
    }

    public byte[] getAvatarData() {
        return avatarData;
    }

    public String getAvatarContentType() {
        return avatarContentType;
    }

    public long getAvatarVersion() {
        return avatarVersion == null ? 0L : avatarVersion;
    }

    public ImageFramingProfilesResponse getAvatarImageFraming() {
        ImageFraming avatar = getOrCreateAvatarImageFraming();
        ImageFraming card = getEffectiveCardImageFraming(avatar);

        return new ImageFramingProfilesResponse(
                ImageFramingResponse.from(avatar),
                ImageFramingResponse.from(card)
        );
    }

    public String getProfileBio() {
        return profileBio;
    }

    public String getProfileLocation() {
        return profileLocation;
    }

    public boolean isShowCars() {
        return Boolean.TRUE.equals(showCars);
    }

    public boolean isShowRaceHistory() {
        return Boolean.TRUE.equals(showRaceHistory);
    }

    public boolean isShowPhotos() {
        return showPhotos == null || showPhotos;
    }

    public MembershipTier getMembershipTier() {
        return membershipTier == null
                ? MembershipTier.STANDARD
                : membershipTier;
    }

    public LocalDateTime getMembershipExpiresAt() {
        return membershipExpiresAt;
    }

    public UserPhoto getProfilePhoto() {
        return profilePhoto;
    }

    public Role getRole() {
        return role;
    }

    public List<RaceCar> getRaceCars() {
        return raceCars;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLicenseCategory(String licenseCategory) {
        this.licenseCategory = licenseCategory;
    }

    public void setLicenseVerified(boolean licenseVerified) {
        this.licenseVerified = licenseVerified;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAvatarData(byte[] avatarData) {
        this.avatarData = avatarData;
    }

    public void setAvatarContentType(String avatarContentType) {
        this.avatarContentType = avatarContentType;
    }

    public void setAvatarVersion(long avatarVersion) {
        this.avatarVersion = avatarVersion;
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

    public void setProfileBio(String profileBio) {
        this.profileBio = profileBio;
    }

    public void setProfileLocation(String profileLocation) {
        this.profileLocation = profileLocation;
    }

    public void setShowCars(boolean showCars) {
        this.showCars = showCars;
    }

    public void setShowRaceHistory(boolean showRaceHistory) {
        this.showRaceHistory = showRaceHistory;
    }

    public void setShowPhotos(boolean showPhotos) {
        this.showPhotos = showPhotos;
    }

    public void setMembershipTier(MembershipTier membershipTier) {
        this.membershipTier = membershipTier;
    }

    public void setMembershipExpiresAt(LocalDateTime membershipExpiresAt) {
        this.membershipExpiresAt = membershipExpiresAt;
    }

    public void setProfilePhoto(UserPhoto profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setRaceCars(List<RaceCar> raceCars) {
        this.raceCars = raceCars;
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
