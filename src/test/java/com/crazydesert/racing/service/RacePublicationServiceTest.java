package com.crazydesert.racing.service;

import com.crazydesert.racing.DesertLiveItem;
import com.crazydesert.racing.Race;
import com.crazydesert.racing.User;
import com.crazydesert.racing.enums.DesertLiveCategory;
import com.crazydesert.racing.enums.DesertLiveModerationStatus;
import com.crazydesert.racing.enums.DesertLiveSource;
import com.crazydesert.racing.enums.RaceStatus;
import com.crazydesert.racing.repository.DesertLiveItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RacePublicationServiceTest {

    @Mock
    private DesertLiveItemRepository itemRepository;

    private RacePublicationService publicationService;

    @BeforeEach
    void setUp() {
        publicationService = new RacePublicationService(itemRepository);
    }

    @Test
    void createsApprovedSystemPublicationLinkedToRace() {
        Race race = createRace();
        User administrator = createAdministrator();

        when(itemRepository.findByLinkedRaceId(7L))
                .thenReturn(Optional.empty());
        when(itemRepository.save(any(DesertLiveItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DesertLiveItem item = publicationService.synchronizePublication(
                race,
                administrator
        );

        assertEquals(DesertLiveCategory.RACE, item.getCategory());
        assertEquals(DesertLiveSource.SYSTEM, item.getSource());
        assertEquals(
                DesertLiveModerationStatus.APPROVED,
                item.getModerationStatus()
        );
        assertNull(item.getTargetUrl());
        assertSame(race, item.getLinkedRace());
        assertSame(administrator, item.getCreatedBy());
    }

    @Test
    void synchronizesTheExistingPublicationWithoutCreatingADuplicate() {
        Race race = createRace();
        User administrator = createAdministrator();
        DesertLiveItem existingItem = new DesertLiveItem();
        ReflectionTestUtils.setField(existingItem, "id", 12L);
        existingItem.setCreatedBy(administrator);
        existingItem.setTitle("Old name");

        when(itemRepository.findByLinkedRaceId(7L))
                .thenReturn(Optional.of(existingItem));
        when(itemRepository.save(existingItem)).thenReturn(existingItem);

        DesertLiveItem item = publicationService.synchronizePublication(
                race,
                administrator
        );

        assertSame(existingItem, item);
        assertEquals("Negev Challenge", item.getTitle());
        verify(itemRepository).save(existingItem);
    }

    @Test
    void deletesPublicationForRace() {
        DesertLiveItem item = new DesertLiveItem();

        when(itemRepository.findByLinkedRaceId(7L))
                .thenReturn(Optional.of(item));

        publicationService.deletePublication(7L);

        verify(itemRepository).delete(item);
        verify(itemRepository).flush();
    }

    @Test
    void deletingRaceWithoutPublicationStillFlushesSafely() {
        when(itemRepository.findByLinkedRaceId(7L))
                .thenReturn(Optional.empty());

        publicationService.deletePublication(7L);

        verify(itemRepository, never()).delete(any());
        verify(itemRepository).flush();
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
        return user;
    }
}
