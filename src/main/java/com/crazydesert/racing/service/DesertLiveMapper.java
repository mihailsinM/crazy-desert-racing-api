package com.crazydesert.racing.service;

import com.crazydesert.racing.DesertLiveItem;
import com.crazydesert.racing.Race;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.DesertLiveItemResponse;
import com.crazydesert.racing.dto.DesertLivePageResponse;
import com.crazydesert.racing.dto.ImageFramingProfilesResponse;
import com.crazydesert.racing.dto.ImageFramingResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class DesertLiveMapper {

    public DesertLivePageResponse toPageResponse(Page<DesertLiveItem> page) {
        return new DesertLivePageResponse(
                page.getContent().stream()
                        .map(this::toResponse)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public DesertLiveItemResponse toResponse(DesertLiveItem item) {
        User author = item.getCreatedBy();
        Race linkedRace = item.getLinkedRace();
        ImageFramingProfilesResponse imageFraming =
                buildImageFraming(item, linkedRace);
        ImageFramingResponse cardFraming = imageFraming.card();

        return new DesertLiveItemResponse(
                item.getId(),
                item.getCategory(),
                item.getSource(),
                item.getModerationStatus(),
                item.getTitle(),
                item.getDescription(),
                buildTargetUrl(item, linkedRace),
                linkedRace == null ? null : linkedRace.getId(),
                author.getId(),
                author.getName(),
                buildAuthorAvatarUrl(author),
                item.getModerationNote(),
                item.getActiveFrom(),
                item.getActiveUntil(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                cardFraming.focusX(),
                cardFraming.focusY(),
                cardFraming.cropPercent(),
                buildItemImageUrl(item, linkedRace),
                imageFraming
        );
    }

    private String buildTargetUrl(
            DesertLiveItem item,
            Race linkedRace) {

        if (linkedRace != null) {
            return "/races/" + linkedRace.getId();
        }

        return item.getTargetUrl();
    }

    private String buildAuthorAvatarUrl(User author) {
        if (author.getAvatarContentType() == null) {
            return null;
        }

        return "/avatars/"
                + author.getId()
                + "?v="
                + author.getAvatarVersion();
    }

    private String buildItemImageUrl(
            DesertLiveItem item,
            Race linkedRace) {

        if (linkedRace != null) {
            return linkedRace.getImageUrl();
        }

        if (item.getImageKey() == null) {
            return null;
        }

        return "/desert-live/images/"
                + item.getImageKey()
                + "?v="
                + item.getImageVersion();
    }

    private ImageFramingProfilesResponse buildImageFraming(
            DesertLiveItem item,
            Race linkedRace) {

        if (linkedRace != null) {
            return linkedRace.getImageFraming();
        }

        ImageFramingResponse cardFraming =
                ImageFramingResponse.from(item.getCardImageFraming());
        ImageFramingResponse avatarFraming =
                ImageFramingResponse.from(item.getAvatarImageFraming());

        return new ImageFramingProfilesResponse(
                avatarFraming,
                cardFraming
        );
    }
}
