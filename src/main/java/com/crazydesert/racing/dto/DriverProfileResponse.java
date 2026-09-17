package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.MembershipTier;
import com.crazydesert.racing.enums.Role;

import java.util.List;

public record DriverProfileResponse(
        Long id,
        String name,
        String avatarUrl,
        ImageFramingProfilesResponse imageFraming,
        String cardImageUrl,
        ImageFramingProfilesResponse cardImageFraming,
        Role role,
        boolean verifiedDriver,
        MembershipTier membershipTier,
        String bio,
        String location,
        boolean carsVisible,
        boolean raceHistoryVisible,
        boolean photosVisible,
        List<DriverCarResponse> cars,
        List<DriverRaceResponse> races,
        List<UserPhotoResponse> photos) {
}
