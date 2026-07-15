package com.example.kservertask.menu.response;

import java.util.List;

public record MenuListResponse(List<MenuResponse> menus) {

    public MenuListResponse {
        menus = List.copyOf(menus);
    }
}
