package com.example.kservertask.menu.response;

import com.example.kservertask.menu.result.MenuResult;

public record MenuResponse(
        Long menuId,
        Long categoryId,
        String categoryName,
        String name,
        long price
) {

    public static MenuResponse from(MenuResult result) {
        return new MenuResponse(
                result.menuId(),
                result.categoryId(),
                result.categoryName(),
                result.name(),
                result.price()
        );
    }
}
