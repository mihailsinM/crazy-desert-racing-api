package com.crazydesert.racing.dto;

import java.util.List;

public record DesertLivePageResponse(
        List<DesertLiveItemResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
}
