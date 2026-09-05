package com.crazydesert.racing.service;

import com.crazydesert.racing.MediaImage;
import com.crazydesert.racing.Race;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.ImageFramingProfileRequest;
import com.crazydesert.racing.dto.ImageFramingRequest;
import com.crazydesert.racing.dto.RaceCreateRequest;
import com.crazydesert.racing.dto.RaceResponse;
import com.crazydesert.racing.dto.RaceUpdateRequest;
import com.crazydesert.racing.enums.MediaImageVisibility;
import com.crazydesert.racing.enums.RaceStatus;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.exception.InvalidImageFramingException;
import com.crazydesert.racing.repository.RaceRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RaceServiceTest {

    @Mock
    private RaceRepository raceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MediaImageService mediaImageService;

    @Mock
    private RacePublicationService racePublicationService;

    private RaceService raceService;

    @BeforeEach
    void setUp() {
        raceService = new RaceService(
                raceRepository,
                userRepository,
                new ImageFramingValidator(new ImageFocusValidator()),
                mediaImageService,
                racePublicationService,
                new RaceMapper()
        );
    }

    @Test
    void createsRaceAndItsLinkedPublicationTogether() {
        User administrator = createAdministrator();
        RaceCreateRequest request = createRequest();

        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(administrator));
        when(raceRepository.save(any(Race.class)))
                .thenAnswer(invocation -> {
                    Race race = invocation.getArgument(0);
                    ReflectionTestUtils.setField(race, "id", 7L);
                    return race;
                });

        RaceResponse response = raceService.createRace(
                "admin@example.com",
                request
        );

        assertEquals(7L, response.id());
        assertEquals(RaceStatus.UPCOMING, response.status());
        assertNull(response.imageUrl());
        assertEquals(50, response.imageFraming().avatar().focusX());
        assertEquals(50, response.imageFraming().card().focusX());
        verify(racePublicationService).synchronizePublication(
                any(Race.class),
                any(User.class)
        );
    }

    @Test
    void updatesRaceAndSynchronizesExistingPublication() {
        User administrator = createAdministrator();
        Race race = createRace();
        RaceUpdateRequest request = new RaceUpdateRequest();
        request.name = "Updated race";
        request.location = "Mitzpe Ramon";
        request.startDate = LocalDate.of(2026, 11, 2);
        request.maxParticipants = 80;
        request.status = RaceStatus.POSTPONED;
        request.adminMessage = "New date announced";

        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(administrator));
        when(raceRepository.findById(7L)).thenReturn(Optional.of(race));
        when(raceRepository.save(race)).thenReturn(race);

        RaceResponse response = raceService.updateRace(
                "admin@example.com",
                7L,
                request
        );

        assertEquals("Updated race", response.name());
        assertEquals(RaceStatus.POSTPONED, response.status());
        verify(racePublicationService).synchronizePublication(
                race,
                administrator
        );
    }

    @Test
    void synchronizesPublicationsForExistingRaces() {
        User administrator = createAdministrator();
        Race firstRace = createRace();
        Race secondRace = createRace();
        ReflectionTestUtils.setField(secondRace, "id", 8L);

        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(administrator));
        when(raceRepository.findAll())
                .thenReturn(List.of(firstRace, secondRace));

        int synchronizedRaces = raceService.synchronizeRacePublications(
                "admin@example.com"
        );

        assertEquals(2, synchronizedRaces);
        verify(racePublicationService).synchronizePublication(
                firstRace,
                administrator
        );
        verify(racePublicationService).synchronizePublication(
                secondRace,
                administrator
        );
    }

    @Test
    void uploadsOnePublicMediaImageWithDifferentProfiles() {
        Race race = createRace();
        MockMultipartFile image = new MockMultipartFile(
                "file",
                "race.webp",
                "image/webp",
                new byte[]{1, 2, 3}
        );
        MediaImage mediaImage = new MediaImage();
        mediaImage.setImageKey("race-image-key");
        mediaImage.setImageVersion(123L);
        ImageFramingRequest framing = createFramingRequest();

        when(raceRepository.findById(7L)).thenReturn(Optional.of(race));
        when(mediaImageService.storeImage(
                null,
                image,
                MediaImageVisibility.PUBLIC
        )).thenReturn(mediaImage);
        when(raceRepository.save(race)).thenReturn(race);

        RaceResponse response = raceService.updateRaceImage(
                7L,
                image,
                framing
        );

        assertEquals(
                "/media/images/race-image-key?v=123",
                response.imageUrl()
        );
        assertEquals(20, response.imageFraming().avatar().focusX());
        assertEquals(15, response.imageFraming().avatar().cropPercent());
        assertEquals(75, response.imageFraming().card().focusX());
        assertEquals(5, response.imageFraming().card().cropPercent());
        verify(mediaImageService).storeImage(
                null,
                image,
                MediaImageVisibility.PUBLIC
        );
    }

    @Test
    void rejectsAnIncompleteExplicitFramingRequest() {
        Race race = createRace();
        ImageFramingRequest request = new ImageFramingRequest();
        request.avatar = new ImageFramingProfileRequest(20, 30, 10);

        when(raceRepository.findById(7L)).thenReturn(Optional.of(race));

        assertThrows(
                InvalidImageFramingException.class,
                () -> raceService.updateRaceImage(
                        7L,
                        new MockMultipartFile(
                                "file",
                                "race.jpg",
                                "image/jpeg",
                                new byte[]{1}
                        ),
                        request
                )
        );
    }

    @Test
    void replacesImageThroughTheExistingMediaImageRecord() {
        Race race = createRace();
        race.setImageKey("race-image-key");
        race.setImageVersion(123L);
        MockMultipartFile image = new MockMultipartFile(
                "file",
                "replacement.png",
                "image/png",
                new byte[]{4, 5, 6}
        );
        MediaImage mediaImage = new MediaImage();
        mediaImage.setImageKey("race-image-key");
        mediaImage.setImageVersion(124L);

        when(raceRepository.findById(7L)).thenReturn(Optional.of(race));
        when(mediaImageService.storeImage(
                "race-image-key",
                image,
                MediaImageVisibility.PUBLIC
        )).thenReturn(mediaImage);
        when(raceRepository.save(race)).thenReturn(race);

        RaceResponse response = raceService.updateRaceImage(
                7L,
                image,
                createFramingRequest()
        );

        assertEquals(
                "/media/images/race-image-key?v=124",
                response.imageUrl()
        );
        verify(mediaImageService).storeImage(
                "race-image-key",
                image,
                MediaImageVisibility.PUBLIC
        );
    }

    @Test
    void updatesFramingWithoutReplacingTheImage() {
        Race race = createRace();
        race.setImageKey("race-image-key");
        race.setImageVersion(123L);
        ImageFramingRequest request = createFramingRequest();

        when(raceRepository.findById(7L)).thenReturn(Optional.of(race));
        when(raceRepository.save(race)).thenReturn(race);

        RaceResponse response = raceService.updateRaceImageFraming(
                7L,
                request
        );

        assertEquals(30, response.imageFraming().avatar().focusY());
        assertEquals(45, response.imageFraming().card().focusY());
        verify(raceRepository).save(race);
    }

    @Test
    void rejectsFramingUpdateBeforeImageUpload() {
        Race race = createRace();

        when(raceRepository.findById(7L)).thenReturn(Optional.of(race));

        assertThrows(
                InvalidImageFramingException.class,
                () -> raceService.updateRaceImageFraming(
                        7L,
                        createFramingRequest()
                )
        );
    }

    @Test
    void deletesRaceImageAndResetsBothProfiles() {
        Race race = createRace();
        race.setImageKey("race-image-key");
        race.setImageVersion(123L);
        race.applyAvatarImageFraming(20, 30, 15);
        race.applyCardImageFraming(75, 45, 5);

        when(raceRepository.findById(7L)).thenReturn(Optional.of(race));
        when(raceRepository.saveAndFlush(race)).thenReturn(race);

        RaceResponse response = raceService.deleteRaceImage(7L);

        assertNull(response.imageUrl());
        assertEquals(50, response.imageFraming().avatar().focusX());
        assertEquals(0, response.imageFraming().avatar().cropPercent());
        assertEquals(50, response.imageFraming().card().focusX());
        assertEquals(0, response.imageFraming().card().cropPercent());
        verify(mediaImageService).deleteImage("race-image-key");
    }

    @Test
    void deletesLinkedPublicationBeforeRaceAndMediaImage() {
        Race race = createRace();
        race.setImageKey("race-image-key");

        when(raceRepository.findById(7L)).thenReturn(Optional.of(race));

        raceService.deleteRaceById(7L);

        verify(racePublicationService).deletePublication(7L);
        verify(raceRepository).delete(race);
        verify(raceRepository).flush();
        verify(mediaImageService).deleteImage("race-image-key");
    }

    private RaceCreateRequest createRequest() {
        RaceCreateRequest request = new RaceCreateRequest();
        request.name = "Negev Challenge";
        request.location = "Negev";
        request.startDate = LocalDate.of(2026, 10, 10);
        request.maxParticipants = 60;
        return request;
    }

    private Race createRace() {
        Race race = new Race();
        ReflectionTestUtils.setField(race, "id", 7L);
        race.setName("Negev Challenge");
        race.setLocation("Negev");
        race.setStartDate(LocalDate.of(2026, 10, 10));
        race.setMaxParticipants(60);
        race.setStatus(RaceStatus.UPCOMING);
        return race;
    }

    private User createAdministrator() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setName("Administrator");
        user.setEmail("admin@example.com");
        user.setRole(Role.ADMIN);
        return user;
    }

    private ImageFramingRequest createFramingRequest() {
        ImageFramingRequest request = new ImageFramingRequest();
        request.avatar = new ImageFramingProfileRequest(20, 30, 15);
        request.card = new ImageFramingProfileRequest(75, 45, 5);
        return request;
    }
}
