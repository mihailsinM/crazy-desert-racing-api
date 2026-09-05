package com.crazydesert.racing.service;

import com.crazydesert.racing.DesertLiveItem;
import com.crazydesert.racing.Race;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.DesertLiveItemResponse;
import com.crazydesert.racing.enums.DesertLiveCategory;
import com.crazydesert.racing.enums.DesertLiveModerationStatus;
import com.crazydesert.racing.enums.DesertLiveSource;
import com.crazydesert.racing.enums.RaceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DesertLiveMapperTest {

    private final DesertLiveMapper mapper = new DesertLiveMapper();

    @Test
    void linkedRacePublicationReusesRaceImageAndFraming() {
        Race race = new Race();
        ReflectionTestUtils.setField(race, "id", 7L);
        race.setName("Negev Challenge");
        race.setLocation("Negev");
        race.setStartDate(LocalDate.of(2026, 10, 10));
        race.setMaxParticipants(60);
        race.setStatus(RaceStatus.UPCOMING);
        race.setImageKey("race-image-key");
        race.setImageVersion(123L);
        race.applyAvatarImageFraming(20, 30, 15);
        race.applyCardImageFraming(75, 45, 5);

        User author = new User();
        ReflectionTestUtils.setField(author, "id", 1L);
        author.setName("Administrator");

        DesertLiveItem item = new DesertLiveItem();
        ReflectionTestUtils.setField(item, "id", 12L);
        item.setCategory(DesertLiveCategory.RACE);
        item.setSource(DesertLiveSource.SYSTEM);
        item.setModerationStatus(DesertLiveModerationStatus.APPROVED);
        item.setTitle("Negev Challenge");
        item.setDescription("Race at Negev");
        item.setCreatedBy(author);
        item.setLinkedRace(race);
        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());

        DesertLiveItemResponse response = mapper.toResponse(item);

        assertEquals(7L, response.linkedRaceId());
        assertEquals("/races/7", response.targetUrl());
        assertEquals(
                "/media/images/race-image-key?v=123",
                response.imageUrl()
        );
        assertEquals(75, response.imageFocusX());
        assertEquals(45, response.imageFocusY());
        assertEquals(5, response.imageCropPercent());
        assertEquals(20, response.imageFraming().avatar().focusX());
        assertEquals(75, response.imageFraming().card().focusX());
    }
}
