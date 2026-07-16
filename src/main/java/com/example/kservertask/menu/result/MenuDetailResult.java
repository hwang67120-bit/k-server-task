package com.example.kservertask.menu.result;

import java.util.List;

public record MenuDetailResult(
        Long menuId,
        Long categoryId,
        String categoryName,
        String name,
        long price,
        List<MenuOptionResult> options
) {

    public MenuDetailResult {
        options = List.copyOf(options);
    }
}
