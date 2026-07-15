package com.example.kservertask.menu.response;

public record PopularMenuResponse(
        int rank,
        Long menuId,
        String name,
        long orderCount
) {
}
