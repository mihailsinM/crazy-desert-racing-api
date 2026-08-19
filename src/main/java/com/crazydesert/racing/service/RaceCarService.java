package com.crazydesert.racing.service;


import com.crazydesert.racing.ImageFraming;
import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.User;
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
            Integer focusX,
            Integer focusY,
            Integer cropPercent) {

        RaceCar raceCar = getManagedRaceCar(currentUserEmail, id);

        validateAndApplyImageFraming(
                raceCar,
                focusX,
                focusY,
                cropPercent
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
            Integer focusX,
            Integer focusY,
            Integer cropPercent) {

        RaceCar raceCar = getManagedRaceCar(currentUserEmail, id);

        if (!raceCar.hasImage()) {
            throw new InvalidImageFramingException(
                    "Upload an image before setting its framing"
            );
        }

        validateAndApplyImageFraming(
                raceCar,
                focusX,
                focusY,
                cropPercent
        );

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

        validateAndApplyImageFraming(
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

        validateAndApplyImageFraming(
                raceCar,
                focusX,
                focusY,
                cropPercent == null
                        ? raceCar.getImageCropPercent()
                        : cropPercent
        );
    }

    private void validateAndApplyImageFraming(
            RaceCar raceCar,
            Integer focusX,
            Integer focusY,
            Integer cropPercent) {

        imageFramingValidator.validate(
                focusX,
                focusY,
                cropPercent
        );
        raceCar.setImageFocusX(focusX);
        raceCar.setImageFocusY(focusY);
        raceCar.setImageCropPercent(cropPercent);
    }

    private void resetImageFraming(RaceCar raceCar) {
        raceCar.setImageFocusX(ImageFraming.DEFAULT_FOCUS);
        raceCar.setImageFocusY(ImageFraming.DEFAULT_FOCUS);
        raceCar.setImageCropPercent(
                ImageFraming.DEFAULT_CROP_PERCENT
        );
    }
}
