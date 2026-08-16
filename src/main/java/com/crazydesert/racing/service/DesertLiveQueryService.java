package com.crazydesert.racing.service;

import com.crazydesert.racing.DesertLiveItem;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.DesertLiveItemResponse;
import com.crazydesert.racing.dto.DesertLivePageResponse;
import com.crazydesert.racing.enums.DesertLiveCategory;
import com.crazydesert.racing.enums.DesertLiveModerationStatus;
import com.crazydesert.racing.enums.DesertLiveSource;
import com.crazydesert.racing.exception.DesertLiveAccessDeniedException;
import com.crazydesert.racing.exception.DesertLiveItemNotFoundException;
import com.crazydesert.racing.exception.InvalidDesertLiveItemException;
import com.crazydesert.racing.exception.UserNotFoundException;
import com.crazydesert.racing.repository.DesertLiveItemRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class DesertLiveQueryService {

    private static final int MAX_RANDOM_ITEMS = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final DesertLiveItemRepository itemRepository;
    private final UserRepository userRepository;
    private final DesertLiveMapper mapper;

    public DesertLiveQueryService(
            DesertLiveItemRepository itemRepository,
            UserRepository userRepository,
            DesertLiveMapper mapper) {

        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    public List<DesertLiveItemResponse> getRandomItems(
            DesertLiveCategory category,
            int limit) {

        if (limit < 1 || limit > MAX_RANDOM_ITEMS) {
            throw new InvalidDesertLiveItemException(
                    "Random item limit must be between 1 and "
                            + MAX_RANDOM_ITEMS
            );
        }

        return itemRepository.findRandomActive(
                        DesertLiveModerationStatus.APPROVED,
                        category,
                        Instant.now(),
                        PageRequest.of(0, limit)
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public DesertLivePageResponse getPublicItems(
            DesertLiveCategory category,
            String search,
            int page,
            int size) {

        Page<DesertLiveItem> items = itemRepository.findActive(
                DesertLiveModerationStatus.APPROVED,
                category,
                normalizeSearch(search),
                Instant.now(),
                createPageable(page, size)
        );

        return mapper.toPageResponse(items);
    }

    public DesertLiveItemResponse getPublicItem(Long id) {
        DesertLiveItem item = getItem(id);

        if (!isPubliclyVisible(item, Instant.now())) {
            throw new DesertLiveItemNotFoundException(
                    "Desert Live item with id " + id + " not found"
            );
        }

        return mapper.toResponse(item);
    }

    public DesertLivePageResponse getMyItems(
            String currentEmail,
            DesertLiveModerationStatus status,
            DesertLiveCategory category,
            String search,
            int page,
            int size) {

        User author = getUserByEmail(currentEmail);
        Page<DesertLiveItem> items = itemRepository.findByAuthor(
                author.getId(),
                status,
                category,
                normalizeSearch(search),
                createPageable(page, size)
        );

        return mapper.toPageResponse(items);
    }

    public DesertLiveItemResponse getMyItem(
            String currentEmail,
            Long id) {

        User author = getUserByEmail(currentEmail);
        return mapper.toResponse(getOwnedUserItem(id, author));
    }

    public DesertLivePageResponse getAdminItems(
            DesertLiveModerationStatus status,
            DesertLiveCategory category,
            String search,
            int page,
            int size) {

        Page<DesertLiveItem> items = itemRepository.findForAdmin(
                status,
                category,
                normalizeSearch(search),
                createPageable(page, size)
        );

        return mapper.toPageResponse(items);
    }

    public DesertLiveItemResponse getAdminItem(Long id) {
        return mapper.toResponse(getItem(id));
    }

    private DesertLiveItem getOwnedUserItem(Long id, User author) {
        DesertLiveItem item = getItem(id);

        if (item.getSource() != DesertLiveSource.USER
                || !Objects.equals(
                item.getCreatedBy().getId(),
                author.getId()
        )) {
            throw new DesertLiveAccessDeniedException(
                    "You can manage only your own Desert Live items"
            );
        }

        return item;
    }

    private DesertLiveItem getItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() ->
                        new DesertLiveItemNotFoundException(
                                "Desert Live item with id "
                                        + id
                                        + " not found"
                        ));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with email " + email + " not found"
                        ));
    }

    private boolean isPubliclyVisible(
            DesertLiveItem item,
            Instant now) {

        return item.getModerationStatus()
                == DesertLiveModerationStatus.APPROVED
                && (item.getActiveFrom() == null
                || !item.getActiveFrom().isAfter(now))
                && (item.getActiveUntil() == null
                || item.getActiveUntil().isAfter(now));
    }

    private Pageable createPageable(int page, int size) {
        if (page < 0) {
            throw new InvalidDesertLiveItemException(
                    "Page number must not be negative"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidDesertLiveItemException(
                    "Page size must be between 1 and " + MAX_PAGE_SIZE
            );
        }

        Sort sort = Sort.by(
                Sort.Order.desc("displayPriority"),
                Sort.Order.desc("createdAt")
        );

        return PageRequest.of(page, size, sort);
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return "";
        }

        String trimmedSearch = search.trim();
        return trimmedSearch.toLowerCase(Locale.ROOT);
    }
}
