package com.crazydesert.racing.service;

import com.crazydesert.racing.MediaImage;
import com.crazydesert.racing.User;
import com.crazydesert.racing.UserPhoto;
import com.crazydesert.racing.UserPhotoReport;
import com.crazydesert.racing.dto.ImageFramingProfileRequest;
import com.crazydesert.racing.dto.ImageFramingRequest;
import com.crazydesert.racing.dto.MediaImageResponse;
import com.crazydesert.racing.dto.PhotoReportReviewRequest;
import com.crazydesert.racing.dto.UserPhotoReportRequest;
import com.crazydesert.racing.dto.UserPhotoReportResponse;
import com.crazydesert.racing.dto.UserPhotoResponse;
import com.crazydesert.racing.dto.UserPhotoUpdateRequest;
import com.crazydesert.racing.enums.MediaImageVisibility;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.enums.UserPhotoReportStatus;
import com.crazydesert.racing.enums.UserPhotoVisibility;
import com.crazydesert.racing.exception.DuplicateUserPhotoReportException;
import com.crazydesert.racing.exception.InvalidImageFramingException;
import com.crazydesert.racing.exception.InvalidUserPhotoException;
import com.crazydesert.racing.exception.UserNotFoundException;
import com.crazydesert.racing.exception.UserPhotoAccessDeniedException;
import com.crazydesert.racing.exception.UserPhotoNotFoundException;
import com.crazydesert.racing.repository.UserPhotoReportRepository;
import com.crazydesert.racing.repository.UserPhotoRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class UserPhotoService {

    private static final int MAX_PHOTOS_PER_USER = 50;

    private final UserRepository userRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final UserPhotoReportRepository userPhotoReportRepository;
    private final MediaImageService mediaImageService;
    private final ImageFramingValidator imageFramingValidator;

    public UserPhotoService(
            UserRepository userRepository,
            UserPhotoRepository userPhotoRepository,
            UserPhotoReportRepository userPhotoReportRepository,
            MediaImageService mediaImageService,
            ImageFramingValidator imageFramingValidator) {

        this.userRepository = userRepository;
        this.userPhotoRepository = userPhotoRepository;
        this.userPhotoReportRepository = userPhotoReportRepository;
        this.mediaImageService = mediaImageService;
        this.imageFramingValidator = imageFramingValidator;
    }

    @Transactional(readOnly = true)
    public List<UserPhotoResponse> getCurrentUserPhotos(String currentEmail) {
        User user = requireUser(currentEmail);

        return userPhotoRepository
                .findByOwnerIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(photo -> toPhotoResponse(user, photo))
                .toList();
    }

    public UserPhotoResponse uploadPhoto(
            String currentEmail,
            MultipartFile image,
            boolean rightsConfirmed,
            String caption,
            UserPhotoVisibility visibility,
            ImageFramingRequest framingRequest) {

        User user = requireUser(currentEmail);

        if (!rightsConfirmed) {
            throw new InvalidUserPhotoException(
                    "Confirm that you own the photo or have permission to publish it"
            );
        }

        if (userPhotoRepository.countByOwnerId(user.getId())
                >= MAX_PHOTOS_PER_USER) {
            throw new InvalidUserPhotoException(
                    "A profile can contain at most 50 photos"
            );
        }

        validateCaption(caption);
        validateImageFramingForUpload(framingRequest);

        MediaImage storedImage = mediaImageService.storeImage(
                null,
                image,
                MediaImageVisibility.PRIVATE
        );

        UserPhoto photo = new UserPhoto();
        photo.setOwner(user);
        photo.setImageKey(storedImage.getImageKey());
        photo.setImageVersion(storedImage.getImageVersion());
        photo.setCaption(trimToNull(caption));
        photo.setVisibility(visibility == null
                ? UserPhotoVisibility.MEMBERS_ONLY
                : visibility);
        photo.setRightsConfirmed(true);
        photo.setCreatedAt(LocalDateTime.now());
        applyImageFramingForUpload(photo, framingRequest);

        UserPhoto savedPhoto = userPhotoRepository.save(photo);
        return toPhotoResponse(user, savedPhoto);
    }

    public UserPhotoResponse updatePhoto(
            String currentEmail,
            Long photoId,
            UserPhotoUpdateRequest request) {

        User user = requireUser(currentEmail);
        UserPhoto photo = requireOwnedPhoto(photoId, user);
        validateCaption(request.caption);

        photo.setCaption(trimToNull(request.caption));
        photo.setVisibility(request.visibility);

        if (request.visibility == UserPhotoVisibility.PRIVATE
                && (isProfilePhoto(user, photo)
                || isProfileCardPhoto(user, photo))) {
            clearProfileReferences(user, photo);
        }

        return toPhotoResponse(user, userPhotoRepository.save(photo));
    }

    public UserPhotoResponse updatePhotoFraming(
            String currentEmail,
            Long photoId,
            ImageFramingRequest request) {

        User user = requireUser(currentEmail);
        UserPhoto photo = requireOwnedPhoto(photoId, user);
        applyImageFramingForUpdate(photo, request);

        return toPhotoResponse(user, userPhotoRepository.save(photo));
    }

    public UserPhotoResponse setProfilePhoto(
            String currentEmail,
            Long photoId) {

        User user = requireUser(currentEmail);
        UserPhoto photo = requireOwnedPhoto(photoId, user);

        if (photo.getVisibility() == UserPhotoVisibility.PRIVATE) {
            throw new InvalidUserPhotoException(
                    "A private photo cannot be used as the profile photo"
            );
        }

        // Preserve the previous profile image on the card when changing only
        // the avatar of an account created before the two choices existed.
        if (user.getProfileCardPhoto() == null
                && user.getProfilePhoto() != null) {
            user.setProfileCardPhoto(user.getProfilePhoto());
        }

        user.setProfilePhoto(photo);
        userRepository.save(user);
        return toPhotoResponse(user, photo);
    }

    public UserPhotoResponse setProfileCardPhoto(
            String currentEmail,
            Long photoId) {

        User user = requireUser(currentEmail);
        UserPhoto photo = requireOwnedPhoto(photoId, user);

        if (photo.getVisibility() == UserPhotoVisibility.PRIVATE) {
            throw new InvalidUserPhotoException(
                    "A private photo cannot be used as the profile card"
            );
        }

        user.setProfileCardPhoto(photo);
        userRepository.save(user);
        return toPhotoResponse(user, photo);
    }

    public void deletePhoto(String currentEmail, Long photoId) {
        User user = requireUser(currentEmail);
        UserPhoto photo = requireOwnedPhoto(photoId, user);

        deletePhoto(photo);
    }

    public UserPhotoResponse hidePhotoAsAdmin(
            String adminEmail,
            Long photoId) {

        requireAdmin(adminEmail);
        UserPhoto photo = requirePhoto(photoId);
        User owner = photo.getOwner();
        photo.setVisibility(UserPhotoVisibility.PRIVATE);

        clearProfileReferences(owner, photo);

        return toPhotoResponse(owner, userPhotoRepository.save(photo));
    }

    public void deletePhotoAsAdmin(String adminEmail, Long photoId) {
        requireAdmin(adminEmail);
        deletePhoto(requirePhoto(photoId));
    }

    private void deletePhoto(UserPhoto photo) {
        User user = photo.getOwner();

        clearProfileReferences(user, photo);

        userPhotoReportRepository.deleteByPhotoId(photo.getId());
        userPhotoRepository.delete(photo);
        mediaImageService.deleteImage(photo.getImageKey());
    }

    @Transactional(readOnly = true)
    public MediaImageResponse getPhotoImage(
            Long photoId,
            String viewerEmail) {

        User viewer = requireUser(viewerEmail);
        UserPhoto photo = requirePhoto(photoId);

        if (!canView(photo, viewer)) {
            throw new UserPhotoAccessDeniedException(
                    "You do not have permission to view this photo"
            );
        }

        return mediaImageService.getAuthorizedImage(photo.getImageKey());
    }

    public UserPhotoReportResponse reportPhoto(
            Long photoId,
            String reporterEmail,
            UserPhotoReportRequest request) {

        User reporter = requireUser(reporterEmail);
        UserPhoto photo = requirePhoto(photoId);

        if (!canView(photo, reporter)) {
            throw new UserPhotoAccessDeniedException(
                    "You do not have permission to view this photo"
            );
        }

        if (Objects.equals(photo.getOwner().getId(), reporter.getId())) {
            throw new InvalidUserPhotoException(
                    "You cannot report your own photo"
            );
        }

        if (userPhotoReportRepository.existsByPhotoIdAndReporterId(
                photoId,
                reporter.getId())) {
            throw new DuplicateUserPhotoReportException(
                    "You have already reported this photo"
            );
        }

        UserPhotoReport report = new UserPhotoReport();
        report.setPhoto(photo);
        report.setReporter(reporter);
        report.setReason(request.reason);
        report.setDetails(trimToNull(request.details));
        report.setStatus(UserPhotoReportStatus.OPEN);
        report.setCreatedAt(LocalDateTime.now());

        return toReportResponse(userPhotoReportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<UserPhotoReportResponse> getOpenReports(
            String adminEmail) {

        requireAdmin(adminEmail);
        return userPhotoReportRepository
                .findByStatusOrderByCreatedAtAsc(UserPhotoReportStatus.OPEN)
                .stream()
                .map(this::toReportResponse)
                .toList();
    }

    public UserPhotoReportResponse reviewReport(
            Long reportId,
            String adminEmail,
            PhotoReportReviewRequest request) {

        requireAdmin(adminEmail);

        if (request.status == null
                || request.status == UserPhotoReportStatus.OPEN) {
            throw new InvalidUserPhotoException(
                    "Reviewed reports must be resolved or dismissed"
            );
        }

        UserPhotoReport report = userPhotoReportRepository.findById(reportId)
                .orElseThrow(() -> new UserPhotoNotFoundException(
                        "Photo report with id " + reportId + " not found"
                ));

        report.setStatus(request.status);
        report.setReviewedAt(LocalDateTime.now());
        return toReportResponse(userPhotoReportRepository.save(report));
    }

    private UserPhoto requireOwnedPhoto(Long photoId, User user) {
        UserPhoto photo = requirePhoto(photoId);

        if (!Objects.equals(photo.getOwner().getId(), user.getId())) {
            throw new UserPhotoAccessDeniedException(
                    "Only the photo owner can change or delete it"
            );
        }

        return photo;
    }

    private UserPhoto requirePhoto(Long photoId) {
        return userPhotoRepository.findById(photoId)
                .orElseThrow(() -> new UserPhotoNotFoundException(
                        "User photo with id " + photoId + " not found"
                ));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User with email " + email + " not found"
                ));
    }

    private User requireAdmin(String email) {
        User user = requireUser(email);

        if (!user.getRole().hasAdminAccess()) {
            throw new UserPhotoAccessDeniedException(
                    "Administrator access is required"
            );
        }

        return user;
    }

    private boolean canView(UserPhoto photo, User viewer) {
        return Objects.equals(photo.getOwner().getId(), viewer.getId())
                || photo.getVisibility() != UserPhotoVisibility.PRIVATE;
    }

    private boolean isProfilePhoto(User user, UserPhoto photo) {
        return user.getProfilePhoto() != null
                && Objects.equals(
                        user.getProfilePhoto().getId(),
                        photo.getId()
                );
    }

    private boolean isProfileCardPhoto(User user, UserPhoto photo) {
        return user.getProfileCardPhoto() != null
                && Objects.equals(
                        user.getProfileCardPhoto().getId(),
                        photo.getId()
                );
    }

    private void clearProfileReferences(User user, UserPhoto photo) {
        boolean changed = false;

        if (isProfilePhoto(user, photo)) {
            user.setProfilePhoto(null);
            changed = true;
        }

        if (isProfileCardPhoto(user, photo)) {
            user.setProfileCardPhoto(null);
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }
    }

    private UserPhotoResponse toPhotoResponse(User owner, UserPhoto photo) {
        UserPhoto cardPhoto = owner.getProfileCardPhoto() == null
                ? owner.getProfilePhoto()
                : owner.getProfileCardPhoto();

        return new UserPhotoResponse(
                photo.getId(),
                "/driver-photos/"
                        + photo.getId()
                        + "/image?v="
                        + photo.getImageVersion(),
                photo.getCaption(),
                photo.getVisibility(),
                photo.getCreatedAt(),
                isProfilePhoto(owner, photo),
                cardPhoto != null
                        && Objects.equals(cardPhoto.getId(), photo.getId()),
                photo.getImageFraming()
        );
    }

    private UserPhotoReportResponse toReportResponse(
            UserPhotoReport report) {

        return new UserPhotoReportResponse(
                report.getId(),
                report.getPhoto().getId(),
                report.getPhoto().getOwner().getId(),
                report.getPhoto().getOwner().getName(),
                report.getReporter().getId(),
                report.getReporter().getName(),
                report.getReason(),
                report.getDetails(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getReviewedAt()
        );
    }

    private void validateCaption(String caption) {
        if (caption != null && caption.trim().length() > 300) {
            throw new InvalidUserPhotoException(
                    "Photo caption must be at most 300 characters"
            );
        }
    }

    private void validateImageFramingForUpload(ImageFramingRequest request) {
        if (request == null || !request.hasExplicitProfiles()) {
            Integer focusX = request == null ? null : request.focusX;
            Integer focusY = request == null ? null : request.focusY;
            Integer cropPercent = request == null ? null : request.cropPercent;
            imageFramingValidator.validate(
                    focusX == null ? 50 : focusX,
                    focusY == null ? 50 : focusY,
                    cropPercent == null ? 0 : cropPercent
            );
            return;
        }

        validateExplicitProfiles(request);
    }

    private void applyImageFramingForUpload(
            UserPhoto photo,
            ImageFramingRequest request) {

        if (request != null && request.hasExplicitProfiles()) {
            applyExplicitProfiles(photo, request);
            return;
        }

        int focusX = request == null || request.focusX == null
                ? 50
                : request.focusX;
        int focusY = request == null || request.focusY == null
                ? 50
                : request.focusY;
        int cropPercent = request == null || request.cropPercent == null
                ? 0
                : request.cropPercent;

        photo.applyAvatarImageFraming(focusX, focusY, cropPercent);
        photo.applyCardImageFraming(focusX, focusY, cropPercent);
    }

    private void applyImageFramingForUpdate(
            UserPhoto photo,
            ImageFramingRequest request) {

        if (request != null && request.hasExplicitProfiles()) {
            validateExplicitProfiles(request);
            applyExplicitProfiles(photo, request);
            return;
        }

        if (request == null) {
            throw new InvalidImageFramingException(
                    "Image framing is required"
            );
        }

        imageFramingValidator.validate(
                request.focusX,
                request.focusY,
                request.cropPercent
        );
        photo.applyAvatarImageFraming(
                request.focusX,
                request.focusY,
                request.cropPercent
        );
        photo.applyCardImageFraming(
                request.focusX,
                request.focusY,
                request.cropPercent
        );
    }

    private void validateExplicitProfiles(ImageFramingRequest request) {
        if (request.avatar == null || request.card == null) {
            throw new InvalidImageFramingException(
                    "Both avatar and card image framing profiles are required"
            );
        }

        if (request.hasLegacyProfile()) {
            throw new InvalidImageFramingException(
                    "Use either avatar/card profiles or legacy image framing fields"
            );
        }

        validateProfile(request.avatar);
        validateProfile(request.card);
    }

    private void validateProfile(ImageFramingProfileRequest profile) {
        imageFramingValidator.validate(
                profile.focusX,
                profile.focusY,
                profile.cropPercent
        );
    }

    private void applyExplicitProfiles(
            UserPhoto photo,
            ImageFramingRequest request) {

        photo.applyAvatarImageFraming(
                request.avatar.focusX,
                request.avatar.focusY,
                request.avatar.cropPercent
        );
        photo.applyCardImageFraming(
                request.card.focusX,
                request.card.focusY,
                request.card.cropPercent
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
