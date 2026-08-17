package com.crazydesert.racing.service;

import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.RaceCarCreateRequest;
import com.crazydesert.racing.dto.RaceCarUpdateRequest;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.exception.InvalidImageFocusException;
import com.crazydesert.racing.repository.RaceCarRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RaceCarServiceTest {

    @Mock
    private RaceCarRepository raceCarRepository;

    @Mock
    private UserRepository userRepository;

    private RaceCarService raceCarService;

    @BeforeEach
    void setUp() {
        raceCarService = new RaceCarService(
                raceCarRepository,
                userRepository,
                new ImageFocusValidator()
        );
    }

    @Test
    void createsRaceCarWithCenteredFocusByDefault() {
        RaceCarCreateRequest request = createRequest();
        stubRaceCarSave();

        RaceCar savedRaceCar = raceCarService.createRaceCar(request);

        assertEquals(50, savedRaceCar.getImageFocusX());
        assertEquals(50, savedRaceCar.getImageFocusY());
    }

    @Test
    void createsRaceCarWithCustomFocus() {
        RaceCarCreateRequest request = createRequest();
        request.imageFocusX = 25;
        request.imageFocusY = 75;
        stubRaceCarSave();

        RaceCar savedRaceCar = raceCarService.createRaceCar(request);

        assertEquals(25, savedRaceCar.getImageFocusX());
        assertEquals(75, savedRaceCar.getImageFocusY());
    }

    @Test
    void rejectsIncompleteOrOutOfRangeFocus() {
        RaceCarCreateRequest incompleteRequest = createRequest();
        incompleteRequest.imageFocusX = 25;

        assertThrows(
                InvalidImageFocusException.class,
                () -> raceCarService.createRaceCar(incompleteRequest)
        );

        RaceCarCreateRequest outOfRangeRequest = createRequest();
        outOfRangeRequest.imageFocusX = -1;
        outOfRangeRequest.imageFocusY = 101;

        assertThrows(
                InvalidImageFocusException.class,
                () -> raceCarService.createRaceCar(outOfRangeRequest)
        );
        verifyNoInteractions(raceCarRepository);
    }

    @Test
    void preservesExistingFocusWhenUpdateOmitsCoordinates() {
        User owner = createUser(1L, "owner@example.com");
        RaceCar raceCar = createRaceCar(owner, 25, 75);
        RaceCarUpdateRequest request = updateRequest();

        stubOwnedRaceCar(owner, raceCar);

        RaceCar updatedRaceCar = raceCarService.updateRaceCar(
                owner.getEmail(),
                10L,
                request
        );

        assertEquals(25, updatedRaceCar.getImageFocusX());
        assertEquals(75, updatedRaceCar.getImageFocusY());
    }

    @Test
    void updatesFocusWhenBothCoordinatesAreProvided() {
        User owner = createUser(1L, "owner@example.com");
        RaceCar raceCar = createRaceCar(owner, 50, 50);
        RaceCarUpdateRequest request = updateRequest();
        request.imageFocusX = 20;
        request.imageFocusY = 80;

        stubOwnedRaceCar(owner, raceCar);

        RaceCar updatedRaceCar = raceCarService.updateRaceCar(
                owner.getEmail(),
                10L,
                request
        );

        assertEquals(20, updatedRaceCar.getImageFocusX());
        assertEquals(80, updatedRaceCar.getImageFocusY());
    }

    @Test
    void returnsCenteredFocusForLegacyCarWithoutCoordinates() {
        RaceCar raceCar = new RaceCar();
        ReflectionTestUtils.setField(raceCar, "imageFocusX", null);
        ReflectionTestUtils.setField(raceCar, "imageFocusY", null);

        assertEquals(50, raceCar.getImageFocusX());
        assertEquals(50, raceCar.getImageFocusY());
    }

    private RaceCarCreateRequest createRequest() {
        RaceCarCreateRequest request = new RaceCarCreateRequest();
        request.name = "Desert Storm";
        request.brand = "BMW";
        request.horsePower = 500;
        request.imageUrl = "https://example.com/car.jpg";
        request.imagePosition = "CENTER";

        return request;
    }

    private RaceCarUpdateRequest updateRequest() {
        RaceCarUpdateRequest request = new RaceCarUpdateRequest();
        request.name = "Desert Storm Updated";
        request.brand = "BMW";
        request.horsePower = 520;
        request.imageUrl = "https://example.com/car-updated.jpg";
        request.imagePosition = "CENTER";

        return request;
    }

    private User createUser(Long id, String email) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        user.setName("Racer");
        user.setAge(38);
        user.setEmail(email);
        user.setLicenseCategory("B");
        user.setRole(Role.USER);

        return user;
    }

    private RaceCar createRaceCar(
            User owner,
            int focusX,
            int focusY) {

        RaceCar raceCar = new RaceCar();
        ReflectionTestUtils.setField(raceCar, "id", 10L);
        raceCar.setName("Desert Storm");
        raceCar.setBrand("BMW");
        raceCar.setHorsePower(500);
        raceCar.setImageUrl("https://example.com/car.jpg");
        raceCar.setImagePosition("CENTER");
        raceCar.setImageFocusX(focusX);
        raceCar.setImageFocusY(focusY);
        raceCar.setOwner(owner);

        return raceCar;
    }

    private void stubRaceCarSave() {
        when(raceCarRepository.save(any(RaceCar.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void stubOwnedRaceCar(User owner, RaceCar raceCar) {
        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(raceCarRepository.findById(10L))
                .thenReturn(Optional.of(raceCar));
        when(raceCarRepository.save(raceCar)).thenReturn(raceCar);
    }
}
