package com.crazydesert.racing.service;


import com.crazydesert.racing.ImageFraming;
import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.ImageFramingProfileRequest;
import com.crazydesert.racing.dto.ImageFramingRequest;
import com.crazydesert.racing.dto.RaceCarCreateRequest;
import com.crazydesert.racing.dto.RaceCarUpdateRequest;
import com.crazydesert.racing.enums.MediaImageVisibility;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.exception.InvalidImageFramingException;
import com.crazydesert.racing.exception.RaceCarNotFoundException;
import com.crazydesert.racing.exception.RaceCarOwnershipException;
import com.crazydesert.racing.exception.UserNotFoundException;
import com.crazydesert.racing.repository.RaceCarRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
public class RaceCarService {

    private final RaceCarRepository raceCarRepository;
    private final UserRepository userRepository;
    private final ImageFramingValidator imageFramingValidator;
    private final MediaImageService mediaImageService;

    public RaceCarService(
            RaceCarRepository raceCarRepository,
            UserRepository userRepository,
            ImageFramingValidator imageFramingValidator,
            MediaImageService mediaImageService) {
        this.raceCarRepository = raceCarRepository;
        this.userRepository = userRepository;
        this.imageFramingValidator = imageFramingValidator;
        this.mediaImageService = mediaImageService;
    }

    public RaceCar assignCarToUser(Long userId, Long raceCarId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + userId + " not found"
                        ));

        RaceCar raceCar = raceCarRepository.findById(raceCarId)
                .orElseThrow(() ->
                        new RaceCarNotFoundException(
                                "Race car with id " + raceCarId + " not found"
                        ));

        raceCar.setOwner(user);

        return raceCarRepository.save(raceCar);
    }

    public List<RaceCar> getAllRaceCars() {
        return raceCarRepository.findAll();
    }

    public List<RaceCar> getRaceCarsByOwnerEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with email " + email + " not found"
                        ));

        return raceCarRepository.findByOwnerId(user.getId());
    }

    public RaceCar createRaceCar(RaceCarCreateRequest request) {

        RaceCar raceCar = new RaceCar();

        applyCreateRequest(raceCar, request);

        return raceCarRepository.save(raceCar);
    }

    public RaceCar getRaceCarById(Long id) {
        return raceCarRepository.findById(id)
                .orElseThrow(()->
                        new RaceCarNotFoundException(
                                "Race car with id " + id + " not found"));
    }

    public RaceCar updateRaceCar(
            String currentUserEmail,
            Long id,
            RaceCarUpdateRequest request) {

        User currentUser = getUserByEmail(currentUserEmail);

        RaceCar existingRaceCar = raceCarRepository.findById(id)
                .orElseThrow(() ->
                        new RaceCarNotFoundException(
                                "Race car with id " + id + " not found"
                        ));

        validateCanManageRaceCar(currentUser, existingRaceCar);

        existingRaceCar.setName(request.name);
        existingRaceCar.setBrand(request.brand);
        existingRaceCar.setHorsePower(request.horsePower);
        if (request.imageUrl != null) {
            existingRaceCar.setImageUrl(request.imageUrl);
        }

        if (request.imagePosition != null) {
            existingRaceCar.setImagePosition(request.imagePosition);
        }

        applyImageFramingForUpdate(
                existingRaceCar,
                request.imageFocusX,
                request.imageFocusY,
                request.imageCropPercent
        );
        return raceCarRepository.save(existingRaceCar);
    }

    public RaceCar createMyRaceCar(String email, RaceCarCreateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with email " + email + " not found"
                        ));

        RaceCar raceCar = new RaceCar();

        applyCreateRequest(raceCar, request);

        raceCar.setOwner(user);

        return raceCarRepository.save(raceCar);
    }

    public void deleteRaceCarById(String currentUserEmail, Long id) {
        User currentUser = getUserByEmail(currentUserEmail);

        RaceCar raceCar = raceCarRepository.findById(id)
                .orElseThrow(() ->
                        new RaceCarNotFoundException(
                                "Race car with id " + id + " not found"
                        ));

        validateCanManageRaceCar(currentUser, raceCar);

        String imageKey = raceCar.getImageKey();
        raceCarRepository.delete(raceCar);
        raceCarRepository.flush();
        mediaImageService.deleteImage(imageKey);
    }

    public RaceCar updateRaceCarImage(
            String currentUserEmail,
            Long id,
            MultipartFile image,
            ImageFramingRequest imageFramingRequest) {

        RaceCar raceCar = getManagedRaceCar(currentUserEmail, id);

        applyImageFramingForUpload(
                raceCar,
                imageFramingRequest
        );

        var mediaImage = mediaImageService.storeImage(
                raceCar.getImageKey(),
                image,
                MediaImageVisibility.PUBLIC
        );
        raceCar.setImageKey(mediaImage.getImageKey());
        raceCar.setImageVersion(mediaImage.getImageVersion());
        raceCar.setImageUrl(null);

        return raceCarRepository.save(raceCar);
    }

    public RaceCar updateRaceCarImageFraming(
            String currentUserEmail,
            Long id,
            ImageFramingRequest request) {

        RaceCar raceCar = getManagedRaceCar(currentUserEmail, id);

        if (!raceCar.hasImage()) {
            throw new InvalidImageFramingException(
                    "Upload an image before setting its framing"
            );
        }

        applyImageFramingForUpdate(raceCar, request);

        return raceCarRepository.save(raceCar);
    }

    public RaceCar deleteRaceCarImage(
            String currentUserEmail,
            Long id) {

        RaceCar raceCar = getManagedRaceCar(currentUserEmail, id);
        String imageKey = raceCar.getImageKey();

        raceCar.setImageKey(null);
        raceCar.setImageVersion(System.currentTimeMillis());
        raceCar.setImageUrl(null);
        resetImageFraming(raceCar);

        RaceCar savedRaceCar = raceCarRepository.saveAndFlush(raceCar);
        mediaImageService.deleteImage(imageKey);

        return savedRaceCar;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with email " + email + " not found"
                        ));
    }

    private void validateCanManageRaceCar(User currentUser, RaceCar raceCar) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = raceCar.getOwner() != null
                && raceCar.getOwner().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new RaceCarOwnershipException(
                    "You can manage only your own race cars"
            );
        }
    }

    private RaceCar getManagedRaceCar(
            String currentUserEmail,
            Long id) {

        User currentUser = getUserByEmail(currentUserEmail);
        RaceCar raceCar = getRaceCarById(id);

        validateCanManageRaceCar(currentUser, raceCar);

        return raceCar;
    }

    private void applyCreateRequest(
            RaceCar raceCar,
            RaceCarCreateRequest request) {

        raceCar.setName(request.name);
        raceCar.setBrand(request.brand);
        raceCar.setHorsePower(request.horsePower);
        raceCar.setImageUrl(request.imageUrl);
        raceCar.setImagePosition(
                request.imagePosition == null || request.imagePosition.isBlank()
                        ? "CENTER"
                        : request.imagePosition
        );

        applyImageFramingForCreate(
                raceCar,
                request.imageFocusX,
                request.imageFocusY,
                request.imageCropPercent
        );
    }

    private void applyImageFramingForCreate(
            RaceCar raceCar,
            Integer focusX,
            Integer focusY,
            Integer cropPercent) {

        if (focusX == null && focusY == null) {
            focusX = ImageFraming.DEFAULT_FOCUS;
            focusY = ImageFraming.DEFAULT_FOCUS;
        }

        validateAndApplyLegacyImageFraming(
                raceCar,
                focusX,
                focusY,
                cropPercent == null
                        ? ImageFraming.DEFAULT_CROP_PERCENT
                        : cropPercent
        );
    }

    private void applyImageFramingForUpdate(
            RaceCar raceCar,
            Integer focusX,
            Integer focusY,
            Integer cropPercent) {

        if (focusX == null && focusY == null && cropPercent == null) {
            return;
        }

        if (focusX == null && focusY == null) {
            focusX = raceCar.getImageFocusX();
            focusY = raceCar.getImageFocusY();
        }

        validateAndApplyLegacyImageFraming(
                raceCar,
                focusX,
                focusY,
                cropPercent == null
                        ? raceCar.getImageCropPercent()
                        : cropPercent
        );
    }

    private void applyImageFramingForUpload(
            RaceCar raceCar,
            ImageFramingRequest request) {

        if (request != null && request.hasExplicitProfiles()) {
            validateAndApplyExplicitImageFraming(raceCar, request);
            return;
        }

        Integer focusX = request == null ? null : request.focusX;
        Integer focusY = request == null ? null : request.focusY;
        Integer cropPercent = request == null ? null : request.cropPercent;

        validateAndApplyLegacyImageFraming(
                raceCar,
                focusX == null ? ImageFraming.DEFAULT_FOCUS : focusX,
                focusY == null ? ImageFraming.DEFAULT_FOCUS : focusY,
                cropPercent == null
                        ? ImageFraming.DEFAULT_CROP_PERCENT
                        : cropPercent
        );
    }

    private void applyImageFramingForUpdate(
            RaceCar raceCar,
            ImageFramingRequest request) {

        if (request != null && request.hasExplicitProfiles()) {
            validateAndApplyExplicitImageFraming(raceCar, request);
            return;
        }

        validateAndApplyLegacyImageFraming(
                raceCar,
                request == null ? null : request.focusX,
                request == null ? null : request.focusY,
                request == null ? null : request.cropPercent
        );
    }

    private void validateAndApplyExplicitImageFraming(
            RaceCar raceCar,
            ImageFramingRequest request) {

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

        raceCar.applyAvatarImageFraming(
                request.avatar.focusX,
                request.avatar.focusY,
                request.avatar.cropPercent
        );
        raceCar.applyCardImageFraming(
                request.card.focusX,
                request.card.focusY,
                request.card.cropPercent
        );
    }

    private void validateProfile(ImageFramingProfileRequest profile) {
        imageFramingValidator.validate(
                profile.focusX,
                profile.focusY,
                profile.cropPercent
        );
    }

    private void validateAndApplyLegacyImageFraming(
            RaceCar raceCar,
            Integer focusX,
            Integer focusY,
            Integer cropPercent) {

        imageFramingValidator.validate(
                focusX,
                focusY,
                cropPercent
        );
        raceCar.applyCardImageFraming(focusX, focusY, cropPercent);
        raceCar.applyAvatarImageFraming(focusX, focusY, cropPercent);
    }

    private void resetImageFraming(RaceCar raceCar) {
        raceCar.applyCardImageFraming(
                ImageFraming.DEFAULT_FOCUS,
                ImageFraming.DEFAULT_FOCUS,
                ImageFraming.DEFAULT_CROP_PERCENT
        );
        raceCar.applyAvatarImageFraming(
                ImageFraming.DEFAULT_FOCUS,
                ImageFraming.DEFAULT_FOCUS,
                ImageFraming.DEFAULT_CROP_PERCENT
        );
    }
}
