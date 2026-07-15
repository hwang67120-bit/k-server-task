package com.example.kservertask.menu.response;

public record MenuResponse(
        Long menuId,
        String name,
        long price
) {
}
