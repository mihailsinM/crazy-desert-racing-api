package com.crazydesert.racing.service;

import com.crazydesert.racing.Race;
import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.RaceRegistration;
import com.crazydesert.racing.User;
import com.crazydesert.racing.UserPhoto;
import com.crazydesert.racing.dto.DriverCarResponse;
import com.crazydesert.racing.dto.DriverProfileResponse;
import com.crazydesert.racing.dto.DriverRaceResponse;
import com.crazydesert.racing.dto.DriverSummaryResponse;
import com.crazydesert.racing.dto.ImageFramingProfilesResponse;
import com.crazydesert.racing.dto.PublicProfileUpdateRequest;
import com.crazydesert.racing.dto.UserPhotoResponse;
import com.crazydesert.racing.enums.MembershipTier;
import com.crazydesert.racing.enums.UserPhotoVisibility;
import com.crazydesert.racing.exception.UserNotFoundException;
import com.crazydesert.racing.repository.RaceCarRepository;
import com.crazydesert.racing.repository.RaceRegistrationRepository;
import com.crazydesert.racing.repository.UserPhotoRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class DriverService {

    private static final Set<UserPhotoVisibility> MEMBER_VISIBILITIES = Set.of(
            UserPhotoVisibility.PUBLIC,
            UserPhotoVisibility.MEMBERS_ONLY
    );

    private final UserRepository userRepository;
    private final RaceCarRepository raceCarRepository;
    private final RaceRegistrationRepository raceRegistrationRepository;
    private final UserPhotoRepository userPhotoRepository;

    public DriverService(
            UserRepository userRepository,
            RaceCarRepository raceCarRepository,
            RaceRegistrationRepository raceRegistrationRepository,
            UserPhotoRepository userPhotoRepository) {

        this.userRepository = userRepository;
        this.raceCarRepository = raceCarRepository;
        this.raceRegistrationRepository = raceRegistrationRepository;
        this.userPhotoRepository = userPhotoRepository;
    }

    @Transactional(readOnly = true)
    public List<DriverSummaryResponse> getDrivers(String viewerEmail) {
        requireUser(viewerEmail);

        return userRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(
                        User::getName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                ))
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public DriverProfileResponse getDriver(
            Long driverId,
            String viewerEmail) {

        User viewer = requireUser(viewerEmail);
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Driver with id " + driverId + " not found"
                ));

        return toProfile(driver, Objects.equals(viewer.getId(), driver.getId()));
    }

    @Transactional(readOnly = true)
    public DriverProfileResponse getCurrentDriver(String currentEmail) {
        return toProfile(requireUser(currentEmail), true);
    }

    public DriverProfileResponse updateCurrentProfile(
            String currentEmail,
            PublicProfileUpdateRequest request) {

        User user = requireUser(currentEmail);
        user.setProfileBio(trimToNull(request.bio));
        user.setProfileLocation(trimToNull(request.location));
        user.setShowCars(request.showCars);
        user.setShowRaceHistory(request.showRaceHistory);
        user.setShowPhotos(request.showPhotos);

        return toProfile(userRepository.save(user), true);
    }

    private DriverSummaryResponse toSummary(User user) {
        UserPhoto profilePhoto = visibleProfilePhoto(user, false);
        boolean showCars = user.isShowCars();
        boolean showRaces = user.isShowRaceHistory();
        boolean showPhotos = user.isShowPhotos();

        return new DriverSummaryResponse(
                user.getId(),
                user.getName(),
                buildAvatarUrl(user, profilePhoto),
                getAvatarFraming(user, profilePhoto),
                user.getRole(),
                user.isLicenseVerified(),
                getActiveMembershipTier(user),
                showCars ? raceCarRepository.countByOwnerId(user.getId()) : 0,
                showRaces
                        ? raceRegistrationRepository.countByUserId(user.getId())
                        : 0,
                showPhotos
                        ? userPhotoRepository.countByOwnerIdAndVisibilityIn(
                                user.getId(),
                                MEMBER_VISIBILITIES
                        )
                        : 0
        );
    }

    private DriverProfileResponse toProfile(User user, boolean ownerView) {
        UserPhoto profilePhoto = visibleProfilePhoto(user, ownerView);
        UserPhoto cardPhoto = visibleCardPhoto(user, ownerView, profilePhoto);
        boolean showCars = ownerView || user.isShowCars();
        boolean showRaces = ownerView || user.isShowRaceHistory();
        boolean showPhotos = ownerView || user.isShowPhotos();

        List<DriverCarResponse> cars = showCars
                ? raceCarRepository.findByOwnerId(user.getId())
                        .stream()
                        .map(this::toCarResponse)
                        .toList()
                : List.of();

        List<DriverRaceResponse> races = showRaces
                ? raceRegistrationRepository.findByUserId(user.getId())
                        .stream()
                        .sorted(Comparator.comparing(
                                registration -> registration.getRace() == null
                                        ? null
                                        : registration.getRace().getStartDate(),
                                Comparator.nullsLast(Comparator.reverseOrder())
                        ))
                        .map(this::toRaceResponse)
                        .toList()
                : List.of();

        List<UserPhoto> photos = showPhotos
                ? getVisiblePhotos(user.getId(), ownerView)
                : List.of();

        return new DriverProfileResponse(
                user.getId(),
                user.getName(),
                buildAvatarUrl(user, profilePhoto),
                getAvatarFraming(user, profilePhoto),
                buildAvatarUrl(user, cardPhoto),
                getAvatarFraming(user, cardPhoto),
                user.getRole(),
                user.isLicenseVerified(),
                getActiveMembershipTier(user),
                user.getProfileBio(),
                user.getProfileLocation(),
                user.isShowCars(),
                user.isShowRaceHistory(),
                user.isShowPhotos(),
                cars,
                races,
                photos.stream()
                        .map(photo -> toPhotoResponse(user, photo))
                        .toList()
        );
    }

    private List<UserPhoto> getVisiblePhotos(Long userId, boolean ownerView) {
        if (ownerView) {
            return userPhotoRepository.findByOwnerIdOrderByCreatedAtDesc(userId);
        }

        return userPhotoRepository
                .findByOwnerIdAndVisibilityInOrderByCreatedAtDesc(
                        userId,
                        MEMBER_VISIBILITIES
                );
    }

    private DriverCarResponse toCarResponse(RaceCar raceCar) {
        return new DriverCarResponse(
                raceCar.getId(),
                raceCar.getName(),
                raceCar.getBrand(),
                raceCar.getHorsePower(),
                raceCar.getImageUrl(),
                raceCar.getImageFraming()
        );
    }

    private DriverRaceResponse toRaceResponse(
            RaceRegistration registration) {

        Race race = registration.getRace();
        RaceCar raceCar = registration.getRaceCar();

        return new DriverRaceResponse(
                registration.getId(),
                race == null ? null : race.getId(),
                race == null ? null : race.getName(),
                race == null ? null : race.getLocation(),
                race == null ? null : race.getStartDate(),
                race == null ? null : race.getStatus(),
                race == null ? null : race.getImageUrl(),
                race == null ? null : race.getImageFraming(),
                raceCar == null ? null : raceCar.getId(),
                raceCar == null ? null : raceCar.getName()
        );
    }

    private UserPhotoResponse toPhotoResponse(User owner, UserPhoto photo) {
        UserPhoto profilePhoto = owner.getProfilePhoto();
        UserPhoto cardPhoto = owner.getProfileCardPhoto() == null
                ? profilePhoto
                : owner.getProfileCardPhoto();

        return new UserPhotoResponse(
                photo.getId(),
                buildPhotoUrl(photo),
                photo.getCaption(),
                photo.getVisibility(),
                photo.getCreatedAt(),
                profilePhoto != null
                        && Objects.equals(profilePhoto.getId(), photo.getId()),
                cardPhoto != null
                        && Objects.equals(cardPhoto.getId(), photo.getId()),
                photo.getImageFraming()
        );
    }

    private UserPhoto visibleProfilePhoto(User user, boolean ownerView) {
        UserPhoto profilePhoto = user.getProfilePhoto();

        if (profilePhoto == null
                || (!ownerView
                && profilePhoto.getVisibility() == UserPhotoVisibility.PRIVATE)) {
            return null;
        }

        return profilePhoto;
    }

    private UserPhoto visibleCardPhoto(
            User user,
            boolean ownerView,
            UserPhoto fallbackPhoto) {

        UserPhoto cardPhoto = user.getProfileCardPhoto();

        if (cardPhoto == null) {
            return fallbackPhoto;
        }

        return !ownerView
                && cardPhoto.getVisibility() == UserPhotoVisibility.PRIVATE
                ? null
                : cardPhoto;
    }

    private String buildAvatarUrl(User user, UserPhoto profilePhoto) {
        if (profilePhoto != null) {
            return buildPhotoUrl(profilePhoto);
        }

        if (user.getAvatarContentType() == null) {
            return null;
        }

        return "/avatars/" + user.getId() + "?v=" + user.getAvatarVersion();
    }

    private String buildPhotoUrl(UserPhoto photo) {
        return "/driver-photos/"
                + photo.getId()
                + "/image?v="
                + photo.getImageVersion();
    }

    private ImageFramingProfilesResponse getAvatarFraming(
            User user,
            UserPhoto profilePhoto) {

        return profilePhoto == null
                ? user.getAvatarImageFraming()
                : profilePhoto.getImageFraming();
    }

    private MembershipTier getActiveMembershipTier(User user) {
        MembershipTier tier = user.getMembershipTier();

        if (tier == MembershipTier.STANDARD) {
            return tier;
        }

        LocalDateTime expiresAt = user.getMembershipExpiresAt();
        return expiresAt != null && expiresAt.isAfter(LocalDateTime.now())
                ? tier
                : MembershipTier.STANDARD;
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User with email " + email + " not found"
                ));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
