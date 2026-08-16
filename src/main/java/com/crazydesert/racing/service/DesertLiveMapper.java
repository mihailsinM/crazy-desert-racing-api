package com.crazydesert.racing.service;

import com.crazydesert.racing.DesertLiveItem;
import com.crazydesert.racing.User;
import com.crazydesert.racing.dto.DesertLiveItemResponse;
import com.crazydesert.racing.dto.DesertLivePageResponse;
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

        return new DesertLiveItemResponse(
                item.getId(),
                item.getCategory(),
                item.getSource(),
                item.getModerationStatus(),
                item.getTitle(),
                item.getDescription(),
                item.getTargetUrl(),
                author.getId(),
                author.getName(),
                buildAuthorAvatarUrl(author),
                item.getModerationNote(),
                item.getActiveFrom(),
                item.getActiveUntil(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                buildItemImageUrl(item)
        );
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

    private String buildItemImageUrl(DesertLiveItem item) {
        if (item.getImageKey() == null) {
            return null;
        }

        return "/desert-live/images/"
                + item.getImageKey()
                + "?v="
                + item.getImageVersion();
    }
}
