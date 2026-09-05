package com.crazydesert.racing.service;

import com.crazydesert.racing.DesertLiveItem;
import com.crazydesert.racing.Race;
import com.crazydesert.racing.User;
import com.crazydesert.racing.enums.DesertLiveCategory;
import com.crazydesert.racing.enums.DesertLiveModerationStatus;
import com.crazydesert.racing.enums.DesertLiveSource;
import com.crazydesert.racing.repository.DesertLiveItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class RacePublicationService {

    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    private final DesertLiveItemRepository itemRepository;

    public RacePublicationService(
            DesertLiveItemRepository itemRepository) {

        this.itemRepository = itemRepository;
    }

    public DesertLiveItem synchronizePublication(
            Race race,
            User administrator) {

        DesertLiveItem item = itemRepository
                .findByLinkedRaceId(race.getId())
                .orElseGet(DesertLiveItem::new);
        boolean newItem = item.getId() == null;

        item.setCategory(DesertLiveCategory.RACE);
        item.setSource(DesertLiveSource.SYSTEM);
        item.setModerationStatus(
                DesertLiveModerationStatus.APPROVED
        );
        item.setTitle(race.getName().trim());
        item.setDescription(buildDescription(race));
        item.setTargetUrl(null);
        item.setLinkedRace(race);
        item.setDisplayPriority(0);
        item.setModerationNote(null);
        item.setModeratedByUserId(administrator.getId());
        item.setModeratedAt(Instant.now());
        item.setActiveUntil(null);

        if (newItem) {
            item.setCreatedBy(administrator);
            item.setActiveFrom(Instant.now());
        }

        return itemRepository.save(item);
    }

    public void deletePublication(Long raceId) {
        itemRepository.findByLinkedRaceId(raceId)
                .ifPresent(itemRepository::delete);
        itemRepository.flush();
    }

    private String buildDescription(Race race) {
        String description = "Race at "
                + race.getLocation()
                + " on "
                + race.getStartDate()
                + " · "
                + race.getStatus();
        String adminMessage = race.getAdminMessage();

        if (adminMessage != null && !adminMessage.isBlank()) {
            description += " · " + adminMessage.trim();
        }

        if (description.length() <= MAX_DESCRIPTION_LENGTH) {
            return description;
        }

        return description.substring(0, MAX_DESCRIPTION_LENGTH);
    }
}
