package com.crazydesert.racing.service;

import com.crazydesert.racing.DesertLiveItem;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.DesertLiveItemResponse;
import com.crazydesert.racing.enums.DesertLiveCategory;
import com.crazydesert.racing.enums.DesertLiveModerationStatus;
import com.crazydesert.racing.enums.DesertLiveSource;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.exception.DesertLiveAccessDeniedException;
import com.crazydesert.racing.exception.DesertLiveItemNotFoundException;
import com.crazydesert.racing.exception.InvalidDesertLiveItemException;
import com.crazydesert.racing.repository.DesertLiveItemRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DesertLiveQueryServiceTest {

    @Mock
    private DesertLiveItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    private DesertLiveQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new DesertLiveQueryService(
                itemRepository,
                userRepository,
                new DesertLiveMapper()
        );
    }

    @Test
    void returnsRandomApprovedItemsForDashboard() {
        User author = createUser(1L, "author@example.com");
        DesertLiveItem item = createItem(
                10L,
                author,
                DesertLiveModerationStatus.APPROVED
        );

        when(itemRepository.findRandomActive(
                eq(DesertLiveModerationStatus.APPROVED),
                eq(DesertLiveCategory.RACE),
                any(Instant.class),
                any(Pageable.class)
        )).thenReturn(List.of(item));

        List<DesertLiveItemResponse> response =
                queryService.getRandomItems(
                        DesertLiveCategory.RACE,
                        3
                );

        assertEquals(1, response.size());
        assertEquals(10L, response.get(0).id());
        assertEquals("Desert update", response.get(0).title());
    }

    @Test
    void rejectsInvalidRandomLimit() {
        assertThrows(
                InvalidDesertLiveItemException.class,
                () -> queryService.getRandomItems(null, 0)
        );
    }

    @Test
    void usesTextSearchValueForPublicFeedQuery() {
        when(itemRepository.findActive(
                eq(DesertLiveModerationStatus.APPROVED),
                eq(DesertLiveCategory.FESTIVAL),
                eq("night music"),
                any(Instant.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        queryService.getPublicItems(
                DesertLiveCategory.FESTIVAL,
                "  Night Music  ",
                0,
                20
        );
    }

    @Test
    void usesEmptyTextSearchValueWhenSearchIsMissing() {
        when(itemRepository.findActive(
                eq(DesertLiveModerationStatus.APPROVED),
                eq(null),
                eq(""),
                any(Instant.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        queryService.getPublicItems(null, null, 0, 20);
    }

    @Test
    void hidesPendingItemFromPublicDetails() {
        User author = createUser(1L, "author@example.com");
        DesertLiveItem item = createItem(
                10L,
                author,
                DesertLiveModerationStatus.PENDING
        );

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThrows(
                DesertLiveItemNotFoundException.class,
                () -> queryService.getPublicItem(10L)
        );
    }

    @Test
    void rejectsReadingAnotherUsersPrivateItem() {
        User owner = createUser(1L, "owner@example.com");
        User otherUser = createUser(2L, "other@example.com");
        DesertLiveItem item = createItem(
                10L,
                owner,
                DesertLiveModerationStatus.PENDING
        );

        when(userRepository.findByEmail("other@example.com"))
                .thenReturn(Optional.of(otherUser));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThrows(
                DesertLiveAccessDeniedException.class,
                () -> queryService.getMyItem(
                        "other@example.com",
                        10L
                )
        );
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

    private DesertLiveItem createItem(
            Long id,
            User author,
            DesertLiveModerationStatus status) {

        DesertLiveItem item = new DesertLiveItem();
        ReflectionTestUtils.setField(item, "id", id);
        item.setCategory(DesertLiveCategory.RACE);
        item.setSource(DesertLiveSource.USER);
        item.setModerationStatus(status);
        item.setTitle("Desert update");
        item.setDescription("A new desert activity is available.");
        item.setCreatedBy(author);
        item.setDisplayPriority(0);
        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());

        return item;
    }
}
