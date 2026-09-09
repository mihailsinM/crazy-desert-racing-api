package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.enums.MembershipTier;

import java.time.LocalDateTime;

public class UserResponse {

    public Long id;

    public String name;

    public int age;

    public String email;

    public String licenseCategory;

    public boolean licenseVerified;

    public Role role;

    public String avatarUrl;

    public ImageFramingProfilesResponse imageFraming;

    public MembershipTier membershipTier;

    public LocalDateTime membershipExpiresAt;

    public String profileBio;

    public String profileLocation;

    public boolean showCars;

    public boolean showRaceHistory;

    public boolean showPhotos;
}
