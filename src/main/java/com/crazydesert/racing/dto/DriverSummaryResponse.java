package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.MembershipTier;
import com.crazydesert.racing.enums.Role;

public record DriverSummaryResponse(
        Long id,
        String name,
        String avatarUrl,
        ImageFramingProfilesResponse imageFraming,
        Role role,
        boolean verifiedDriver,
        MembershipTier membershipTier,
        long carCount,
        long raceCount,
        long photoCount) {
}
