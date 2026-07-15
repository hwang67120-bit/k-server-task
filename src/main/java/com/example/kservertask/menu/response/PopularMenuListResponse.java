package com.example.kservertask.menu.response;

import java.time.OffsetDateTime;
import java.util.List;

public record PopularMenuListResponse(
        OffsetDateTime from,
        OffsetDateTime to,
        List<PopularMenuResponse> menus
) {

    public PopularMenuListResponse {
        menus = List.copyOf(menus);
    }
}
