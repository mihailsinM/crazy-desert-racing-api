package com.crazydesert.racing.service;

import com.crazydesert.racing.DesertLiveItem;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.DesertLiveCreateRequest;
import com.crazydesert.racing.dto.DesertLiveItemResponse;
import com.crazydesert.racing.dto.DesertLiveUpdateRequest;
import com.crazydesert.racing.enums.DesertLiveCategory;
import com.crazydesert.racing.enums.DesertLiveModerationStatus;
import com.crazydesert.racing.enums.DesertLiveSource;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.exception.InvalidDesertLiveItemException;
import com.crazydesert.racing.repository.DesertLiveItemRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DesertLiveCommandServiceTest {

    @Mock
    private DesertLiveItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DesertLiveImageService imageService;

    private DesertLiveCommandService commandService;

    @BeforeEach
    void setUp() {
        commandService = new DesertLiveCommandService(
                itemRepository,
                userRepository,
                imageService,
                new DesertLiveMapper()
        );
    }

    @Test
    void createsUserItemAsPending() {
        User author = createUser(1L, "author@example.com", Role.USER);
        DesertLiveCreateRequest request = createRequest();

        when(userRepository.findByEmail("author@example.com"))
                .thenReturn(Optional.of(author));
        when(itemRepository.save(any(DesertLiveItem.class)))
                .thenAnswer(invocation -> {
                    DesertLiveItem item = invocation.getArgument(0);
                    ReflectionTestUtils.setField(item, "id", 10L);
                    return item;
                });

        DesertLiveItemResponse response = commandService.createMyItem(
                "author@example.com",
                request
        );

        assertEquals(DesertLiveSource.USER, response.source());
        assertEquals(
                DesertLiveModerationStatus.PENDING,
                response.moderationStatus()
        );
        assertEquals("/races", response.targetUrl());
    }

    @Test
    void resetsApprovedUserItemToPendingWhenEdited() {
        User author = createUser(1L, "author@example.com", Role.USER);
        DesertLiveItem item = createItem(
                10L,
                author,
                DesertLiveModerationStatus.APPROVED
        );
        item.setModeratedByUserId(2L);
        item.setModerationNote("Old note");
        item.setModeratedAt(Instant.now());

        DesertLiveUpdateRequest request = updateRequest();
        request.title = "Updated title";

        when(userRepository.findByEmail("author@example.com"))
                .thenReturn(Optional.of(author));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        DesertLiveItemResponse response = commandService.updateMyItem(
                "author@example.com",
                10L,
                request
        );

        assertEquals("Updated title", response.title());
        assertEquals(
                DesertLiveModerationStatus.PENDING,
                response.moderationStatus()
        );
        assertNull(item.getModeratedByUserId());
        assertNull(item.getModerationNote());
        assertNull(item.getModeratedAt());
    }

    @Test
    void approvesPendingItemAndStartsItWhenNoStartWasSelected() {
        User author = createUser(1L, "author@example.com", Role.USER);
        User admin = createUser(2L, "admin@example.com", Role.ADMIN);
        DesertLiveItem item = createItem(
                10L,
                author,
                DesertLiveModerationStatus.PENDING
        );

        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(admin));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        DesertLiveItemResponse response = commandService.approveItem(
                "admin@example.com",
                10L
        );

        assertEquals(
                DesertLiveModerationStatus.APPROVED,
                response.moderationStatus()
        );
        assertEquals(2L, item.getModeratedByUserId());
        assertNotNull(item.getModeratedAt());
        assertNotNull(item.getActiveFrom());
    }

    @Test
    void changingImageReturnsApprovedUserItemToPending() {
        User author = createUser(1L, "author@example.com", Role.USER);
        DesertLiveItem item = createItem(
                10L,
                author,
                DesertLiveModerationStatus.APPROVED
        );
        MockMultipartFile image = new MockMultipartFile(
                "file",
                "desert.webp",
                "image/webp",
                new byte[]{1, 2, 3}
        );

        when(userRepository.findByEmail("author@example.com"))
                .thenReturn(Optional.of(author));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        DesertLiveItemResponse response =
                commandService.updateMyItemImage(
                        "author@example.com",
                        10L,
                        image
                );

        verify(imageService).storeImage(item, image);
        assertEquals(
                DesertLiveModerationStatus.PENDING,
                response.moderationStatus()
        );
    }

    @Test
    void rejectsUnsafeTargetUrl() {
        User author = createUser(1L, "author@example.com", Role.USER);
        DesertLiveCreateRequest request = createRequest();
        request.targetUrl = "javascript:alert(1)";

        when(userRepository.findByEmail("author@example.com"))
                .thenReturn(Optional.of(author));

        assertThrows(
                InvalidDesertLiveItemException.class,
                () -> commandService.createMyItem(
                        "author@example.com",
                        request
                )
        );
    }

    private User createUser(Long id, String email, Role role) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        user.setName(role == Role.ADMIN ? "Admin" : "Racer");
        user.setAge(38);
        user.setEmail(email);
        user.setLicenseCategory("B");
        user.setRole(role);

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

    private DesertLiveCreateRequest createRequest() {
        DesertLiveCreateRequest request = new DesertLiveCreateRequest();
        request.category = DesertLiveCategory.RACE;
        request.title = "Desert update";
        request.description = "A new desert activity is available.";
        request.targetUrl = "/races";

        return request;
    }

    private DesertLiveUpdateRequest updateRequest() {
        DesertLiveUpdateRequest request = new DesertLiveUpdateRequest();
        request.category = DesertLiveCategory.RACE;
        request.title = "Desert update";
        request.description = "A new desert activity is available.";
        request.targetUrl = "/races";

        return request;
    }
}
