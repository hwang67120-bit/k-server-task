package com.example.kservertask.menu.controller;

import com.example.kservertask.menu.response.MenuDetailResponse;
import com.example.kservertask.menu.response.MenuListResponse;
import com.example.kservertask.menu.result.MenuDetailResult;
import com.example.kservertask.menu.result.MenuResult;
import com.example.kservertask.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public MenuListResponse getMenus() {
        List<MenuResult> results = menuService.getMenus();
        return MenuListResponse.from(results);
    }

    @GetMapping("/{menuId}")
    public MenuDetailResponse getMenu(@PathVariable Long menuId) {
        MenuDetailResult result = menuService.getMenu(menuId);
        return MenuDetailResponse.from(result);
    }
}
