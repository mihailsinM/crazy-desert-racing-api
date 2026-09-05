package com.crazydesert.racing.service;

import com.crazydesert.racing.ImageFraming;
import com.crazydesert.racing.Race;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.ImageFramingProfileRequest;
import com.crazydesert.racing.dto.ImageFramingRequest;
import com.crazydesert.racing.dto.RaceCreateRequest;
import com.crazydesert.racing.dto.RaceResponse;
import com.crazydesert.racing.dto.RaceUpdateRequest;
import com.crazydesert.racing.enums.MediaImageVisibility;
import com.crazydesert.racing.enums.RaceStatus;
import com.crazydesert.racing.exception.InvalidImageFramingException;
import com.crazydesert.racing.exception.RaceNotFoundException;
import com.crazydesert.racing.exception.UserNotFoundException;
import com.crazydesert.racing.repository.RaceRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
public class RaceService {

    private final RaceRepository raceRepository;
    private final UserRepository userRepository;
    private final ImageFramingValidator imageFramingValidator;
    private final MediaImageService mediaImageService;
    private final RacePublicationService racePublicationService;
    private final RaceMapper raceMapper;

    public RaceService(
            RaceRepository raceRepository,
            UserRepository userRepository,
            ImageFramingValidator imageFramingValidator,
            MediaImageService mediaImageService,
            RacePublicationService racePublicationService,
            RaceMapper raceMapper) {

        this.raceRepository = raceRepository;
        this.userRepository = userRepository;
        this.imageFramingValidator = imageFramingValidator;
        this.mediaImageService = mediaImageService;
        this.racePublicationService = racePublicationService;
        this.raceMapper = raceMapper;
    }

    public RaceResponse createRace(
            String currentEmail,
            RaceCreateRequest request) {

        User administrator = getUserByEmail(currentEmail);
        Race race = new Race();

        race.setName(request.name);
        race.setLocation(request.location);
        race.setStartDate(request.startDate);
        race.setMaxParticipants(request.maxParticipants);
        race.setStatus(RaceStatus.UPCOMING);
        race.setAdminMessage(null);
        resetImageFraming(race);

        Race savedRace = raceRepository.save(race);
        racePublicationService.synchronizePublication(
                savedRace,
                administrator
        );

        return raceMapper.toResponse(savedRace);
    }

    @Transactional(readOnly = true)
    public List<RaceResponse> getAllRaces() {
        return raceRepository.findAll().stream()
                .map(raceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RaceResponse getRaceById(Long id) {
        return raceMapper.toResponse(getRaceEntity(id));
    }

    public RaceResponse updateRace(
            String currentEmail,
            Long id,
            RaceUpdateRequest request) {

        User administrator = getUserByEmail(currentEmail);
        Race existingRace = getRaceEntity(id);

        existingRace.setName(request.name);
        existingRace.setLocation(request.location);
        existingRace.setStartDate(request.startDate);
        existingRace.setMaxParticipants(request.maxParticipants);
        existingRace.setStatus(request.status);
        existingRace.setAdminMessage(request.adminMessage);

        Race savedRace = raceRepository.save(existingRace);
        racePublicationService.synchronizePublication(
                savedRace,
                administrator
        );

        return raceMapper.toResponse(savedRace);
    }

    public int synchronizeRacePublications(String currentEmail) {
        User administrator = getUserByEmail(currentEmail);
        List<Race> races = raceRepository.findAll();

        races.forEach(race ->
                racePublicationService.synchronizePublication(
                        race,
                        administrator
                ));

        return races.size();
    }

    public void deleteRaceById(Long id) {
        Race race = getRaceEntity(id);
        String imageKey = race.getImageKey();

        racePublicationService.deletePublication(id);
        raceRepository.delete(race);
        raceRepository.flush();
        mediaImageService.deleteImage(imageKey);
    }

    public RaceResponse updateRaceImage(
            Long id,
            MultipartFile image,
            ImageFramingRequest request) {

        Race race = getRaceEntity(id);
        applyImageFramingForUpload(race, request);

        var mediaImage = mediaImageService.storeImage(
                race.getImageKey(),
                image,
                MediaImageVisibility.PUBLIC
        );
        race.setImageKey(mediaImage.getImageKey());
        race.setImageVersion(mediaImage.getImageVersion());

        return raceMapper.toResponse(raceRepository.save(race));
    }

    public RaceResponse updateRaceImageFraming(
            Long id,
            ImageFramingRequest request) {

        Race race = getRaceEntity(id);

        if (!race.hasImage()) {
            throw new InvalidImageFramingException(
                    "Upload an image before setting its framing"
            );
        }

        applyImageFramingForUpdate(race, request);

        return raceMapper.toResponse(raceRepository.save(race));
    }

    public RaceResponse deleteRaceImage(Long id) {
        Race race = getRaceEntity(id);
        String imageKey = race.getImageKey();

        race.setImageKey(null);
        race.setImageVersion(System.currentTimeMillis());
        resetImageFraming(race);

        Race savedRace = raceRepository.saveAndFlush(race);
        mediaImageService.deleteImage(imageKey);

        return raceMapper.toResponse(savedRace);
    }

    private Race getRaceEntity(Long id) {
        return raceRepository.findById(id)
                .orElseThrow(() ->
                        new RaceNotFoundException(
                                "Race with id " + id + " not found"
                        ));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with email " + email + " not found"
                        ));
    }

    private void applyImageFramingForUpload(
            Race race,
            ImageFramingRequest request) {

        if (request != null && request.hasExplicitProfiles()) {
            validateAndApplyExplicitImageFraming(race, request);
            return;
        }

        Integer focusX = request == null ? null : request.focusX;
        Integer focusY = request == null ? null : request.focusY;
        Integer cropPercent = request == null ? null : request.cropPercent;

        validateAndApplyLegacyImageFraming(
                race,
                focusX == null ? ImageFraming.DEFAULT_FOCUS : focusX,
                focusY == null ? ImageFraming.DEFAULT_FOCUS : focusY,
                cropPercent == null
                        ? ImageFraming.DEFAULT_CROP_PERCENT
                        : cropPercent
        );
    }

    private void applyImageFramingForUpdate(
            Race race,
            ImageFramingRequest request) {

        if (request != null && request.hasExplicitProfiles()) {
            validateAndApplyExplicitImageFraming(race, request);
            return;
        }

        validateAndApplyLegacyImageFraming(
                race,
                request == null ? null : request.focusX,
                request == null ? null : request.focusY,
                request == null ? null : request.cropPercent
        );
    }

    private void validateAndApplyExplicitImageFraming(
            Race race,
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

        race.applyAvatarImageFraming(
                request.avatar.focusX,
                request.avatar.focusY,
                request.avatar.cropPercent
        );
        race.applyCardImageFraming(
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
            Race race,
            Integer focusX,
            Integer focusY,
            Integer cropPercent) {

        imageFramingValidator.validate(
                focusX,
                focusY,
                cropPercent
        );
        race.applyCardImageFraming(focusX, focusY, cropPercent);
        race.applyAvatarImageFraming(focusX, focusY, cropPercent);
    }

    private void resetImageFraming(Race race) {
        race.applyCardImageFraming(
                ImageFraming.DEFAULT_FOCUS,
                ImageFraming.DEFAULT_FOCUS,
                ImageFraming.DEFAULT_CROP_PERCENT
        );
        race.applyAvatarImageFraming(
                ImageFraming.DEFAULT_FOCUS,
                ImageFraming.DEFAULT_FOCUS,
                ImageFraming.DEFAULT_CROP_PERCENT
        );
    }
}
