package com.example.kservertask.menu.response;

import com.example.kservertask.menu.result.MenuResult;

import java.util.List;

public record MenuListResponse(List<MenuResponse> menus) {

    public MenuListResponse {
        menus = List.copyOf(menus);
    }

    public static MenuListResponse from(List<MenuResult> results) {
        List<MenuResponse> menus = results.stream()
                .map(MenuResponse::from)
                .toList();

        return new MenuListResponse(menus);
    }
}
