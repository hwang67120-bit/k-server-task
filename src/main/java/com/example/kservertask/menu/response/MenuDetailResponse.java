package com.example.kservertask.menu.response;

import com.example.kservertask.menu.result.MenuDetailResult;

import java.util.List;

public record MenuDetailResponse(
        Long menuId,
        Long categoryId,
        String categoryName,
        String name,
        long price,
        List<MenuOptionResponse> options
) {

    public MenuDetailResponse {
        options = List.copyOf(options);
    }

    public static MenuDetailResponse from(MenuDetailResult result) {
        List<MenuOptionResponse> options = result.options().stream()
                .map(MenuOptionResponse::from)
                .toList();

        return new MenuDetailResponse(
                result.menuId(),
                result.categoryId(),
                result.categoryName(),
                result.name(),
                result.price(),
                options
        );
    }
}
