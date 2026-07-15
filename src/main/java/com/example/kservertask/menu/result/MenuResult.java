package com.example.kservertask.menu.result;

public record MenuResult(
        Long menuId,
        Long categoryId,
        String categoryName,
        String name,
        long price
) {
}
