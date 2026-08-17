package com.crazydesert.racing.service;

import com.crazydesert.racing.DesertLiveItem;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.DesertLiveCreateRequest;
import com.crazydesert.racing.dto.DesertLiveItemResponse;
import com.crazydesert.racing.dto.DesertLiveUpdateRequest;
import com.crazydesert.racing.enums.DesertLiveModerationStatus;
import com.crazydesert.racing.enums.DesertLiveSource;
import com.crazydesert.racing.exception.DesertLiveAccessDeniedException;
import com.crazydesert.racing.exception.DesertLiveItemNotFoundException;
import com.crazydesert.racing.exception.InvalidDesertLiveItemException;
import com.crazydesert.racing.exception.InvalidImageFocusException;
import com.crazydesert.racing.exception.UserNotFoundException;
import com.crazydesert.racing.repository.DesertLiveItemRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Service
@Transactional
public class DesertLiveCommandService {

    private final DesertLiveItemRepository itemRepository;
    private final UserRepository userRepository;
    private final DesertLiveImageService imageService;
    private final ImageFocusValidator imageFocusValidator;
    private final DesertLiveMapper mapper;

    public DesertLiveCommandService(
            DesertLiveItemRepository itemRepository,
            UserRepository userRepository,
            DesertLiveImageService imageService,
            ImageFocusValidator imageFocusValidator,
            DesertLiveMapper mapper) {

        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.imageFocusValidator = imageFocusValidator;
        this.mapper = mapper;
    }

    public DesertLiveItemResponse createMyItem(
            String currentEmail,
            DesertLiveCreateRequest request) {

        User author = getUserByEmail(currentEmail);
        DesertLiveItem item = new DesertLiveItem();

        applyCreateRequest(item, request);
        item.setSource(DesertLiveSource.USER);
        item.setModerationStatus(DesertLiveModerationStatus.PENDING);
        item.setDisplayPriority(0);
        item.setCreatedBy(author);

        return mapper.toResponse(itemRepository.save(item));
    }

    public DesertLiveItemResponse updateMyItem(
            String currentEmail,
            Long id,
            DesertLiveUpdateRequest request) {

        DesertLiveItem item = getOwnedUserItem(id, currentEmail);

        applyUpdateRequest(item, request);
        resetUserItemToPending(item);

        return mapper.toResponse(itemRepository.save(item));
    }

    public void deleteMyItem(String currentEmail, Long id) {
        DesertLiveItem item = getOwnedUserItem(id, currentEmail);

        imageService.deleteImageIfPresent(item.getId());
        itemRepository.delete(item);
    }

    public DesertLiveItemResponse updateMyItemImage(
            String currentEmail,
            Long id,
            MultipartFile image,
            int focusX,
            int focusY) {

        DesertLiveItem item = getOwnedUserItem(id, currentEmail);

        imageFocusValidator.validate(focusX, focusY);
        imageService.storeImage(item, image);
        applyImageFocus(item, focusX, focusY);
        resetUserItemToPending(item);

        return mapper.toResponse(itemRepository.save(item));
    }

    public DesertLiveItemResponse deleteMyItemImage(
            String currentEmail,
            Long id) {

        DesertLiveItem item = getOwnedUserItem(id, currentEmail);

        imageService.deleteImage(item);
        resetImageFocus(item);
        resetUserItemToPending(item);

        return mapper.toResponse(itemRepository.save(item));
    }

    public DesertLiveItemResponse createAdminItem(
            String currentEmail,
            DesertLiveCreateRequest request) {

        User admin = getUserByEmail(currentEmail);
        DesertLiveItem item = new DesertLiveItem();

        applyCreateRequest(item, request);
        item.setSource(DesertLiveSource.SYSTEM);
        item.setModerationStatus(DesertLiveModerationStatus.APPROVED);
        item.setDisplayPriority(0);
        item.setCreatedBy(admin);
        item.setModeratedByUserId(admin.getId());
        item.setModeratedAt(Instant.now());

        if (item.getActiveFrom() == null) {
            item.setActiveFrom(Instant.now());
        }

        return mapper.toResponse(itemRepository.save(item));
    }

    public DesertLiveItemResponse updateAdminItem(
            Long id,
            DesertLiveUpdateRequest request) {

        DesertLiveItem item = getItem(id);
        applyUpdateRequest(item, request);

        return mapper.toResponse(itemRepository.save(item));
    }

    public DesertLiveItemResponse approveItem(
            String currentEmail,
            Long id) {

        User admin = getUserByEmail(currentEmail);
        DesertLiveItem item = getItem(id);

        item.setModerationStatus(DesertLiveModerationStatus.APPROVED);
        item.setModerationNote(null);
        item.setModeratedByUserId(admin.getId());
        item.setModeratedAt(Instant.now());

        if (item.getActiveFrom() == null) {
            item.setActiveFrom(Instant.now());
        }

        return mapper.toResponse(itemRepository.save(item));
    }

    public DesertLiveItemResponse rejectItem(
            String currentEmail,
            Long id,
            String reason) {

        User admin = getUserByEmail(currentEmail);
        DesertLiveItem item = getItem(id);

        item.setModerationStatus(DesertLiveModerationStatus.REJECTED);
        item.setModerationNote(reason.trim());
        item.setModeratedByUserId(admin.getId());
        item.setModeratedAt(Instant.now());

        return mapper.toResponse(itemRepository.save(item));
    }

    public DesertLiveItemResponse updateAdminItemImage(
            Long id,
            MultipartFile image,
            int focusX,
            int focusY) {

        DesertLiveItem item = getItem(id);

        imageFocusValidator.validate(focusX, focusY);
        imageService.storeImage(item, image);
        applyImageFocus(item, focusX, focusY);

        return mapper.toResponse(itemRepository.save(item));
    }

    public DesertLiveItemResponse deleteAdminItemImage(Long id) {
        DesertLiveItem item = getItem(id);
        imageService.deleteImage(item);
        resetImageFocus(item);

        return mapper.toResponse(itemRepository.save(item));
    }

    public DesertLiveItemResponse updateMyItemImageFocus(
            String currentEmail,
            Long id,
            int focusX,
            int focusY) {

        DesertLiveItem item = getOwnedUserItem(id, currentEmail);

        updateImageFocus(item, focusX, focusY);

        return mapper.toResponse(itemRepository.save(item));
    }

    public DesertLiveItemResponse updateAdminItemImageFocus(
            Long id,
            int focusX,
            int focusY) {

        DesertLiveItem item = getItem(id);

        updateImageFocus(item, focusX, focusY);

        return mapper.toResponse(itemRepository.save(item));
    }

    public void deleteAdminItem(Long id) {
        DesertLiveItem item = getItem(id);

        imageService.deleteImageIfPresent(item.getId());
        itemRepository.delete(item);
    }

    private void applyCreateRequest(
            DesertLiveItem item,
            DesertLiveCreateRequest request) {

        validateActivePeriod(request.activeFrom, request.activeUntil);

        item.setCategory(request.category);
        item.setTitle(request.title.trim());
        item.setDescription(request.description.trim());
        item.setTargetUrl(normalizeTargetUrl(request.targetUrl));
        item.setActiveFrom(request.activeFrom);
        item.setActiveUntil(request.activeUntil);
    }

    private void applyUpdateRequest(
            DesertLiveItem item,
            DesertLiveUpdateRequest request) {

        validateActivePeriod(request.activeFrom, request.activeUntil);

        item.setCategory(request.category);
        item.setTitle(request.title.trim());
        item.setDescription(request.description.trim());
        item.setTargetUrl(normalizeTargetUrl(request.targetUrl));
        item.setActiveFrom(request.activeFrom);
        item.setActiveUntil(request.activeUntil);
    }

    private void validateActivePeriod(
            Instant activeFrom,
            Instant activeUntil) {

        if (activeFrom != null
                && activeUntil != null
                && !activeUntil.isAfter(activeFrom)) {
            throw new InvalidDesertLiveItemException(
                    "Active-until time must be after active-from time"
            );
        }
    }

    private String normalizeTargetUrl(String targetUrl) {
        String normalizedUrl = trimToNull(targetUrl);

        if (normalizedUrl == null) {
            return null;
        }

        if (normalizedUrl.startsWith("/")
                && !normalizedUrl.startsWith("//")) {
            return normalizedUrl;
        }

        try {
            URI uri = new URI(normalizedUrl);
            String scheme = uri.getScheme();

            if (scheme != null
                    && uri.getHost() != null
                    && (scheme.toLowerCase(Locale.ROOT).equals("http")
                    || scheme.toLowerCase(Locale.ROOT).equals("https"))) {
                return normalizedUrl;
            }
        } catch (URISyntaxException ignored) {
            // A user-facing validation error is returned below.
        }

        throw new InvalidDesertLiveItemException(
                "Target URL must be an internal path or an HTTP/HTTPS URL"
        );
    }

    private void resetUserItemToPending(DesertLiveItem item) {
        item.setModerationStatus(DesertLiveModerationStatus.PENDING);
        item.setModerationNote(null);
        item.setModeratedByUserId(null);
        item.setModeratedAt(null);
    }

    private void updateImageFocus(
            DesertLiveItem item,
            int focusX,
            int focusY) {

        if (item.getImageKey() == null) {
            throw new InvalidImageFocusException(
                    "Upload an image before setting its focus point"
            );
        }

        imageFocusValidator.validate(focusX, focusY);
        applyImageFocus(item, focusX, focusY);
    }

    private void applyImageFocus(
            DesertLiveItem item,
            int focusX,
            int focusY) {

        item.setImageFocusX(focusX);
        item.setImageFocusY(focusY);
    }

    private void resetImageFocus(DesertLiveItem item) {
        applyImageFocus(
                item,
                ImageFocusValidator.DEFAULT_FOCUS,
                ImageFocusValidator.DEFAULT_FOCUS
        );
    }

    private DesertLiveItem getOwnedUserItem(
            Long id,
            String currentEmail) {

        User author = getUserByEmail(currentEmail);
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
