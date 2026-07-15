package com.example.kservertask.menu.service;

import com.example.kservertask.error.BusinessException;
import com.example.kservertask.error.ErrorCode;

import com.example.kservertask.menu.entity.Menu;
import com.example.kservertask.menu.entity.MenuCategory;
import com.example.kservertask.menu.repository.MenuCategoryRepository;
import com.example.kservertask.menu.repository.MenuRepository;
import com.example.kservertask.menu.result.MenuResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;

    public List<MenuResult> getMenus() {
        List<Menu> menus = menuRepository.findAll();

        if (menus.isEmpty()) {
            return List.of();
        }

        Set<Long> categoryIds = menus.stream()
                .map(Menu::getCategoryId)
                .collect(Collectors.toSet());

        Map<Long, MenuCategory> categories = menuCategoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(MenuCategory::getCategoryId, Function.identity()));

        return menus.stream()
                .sorted(Comparator
                        .comparingInt((Menu menu) -> getCategory(categories, menu).getDisplayOrder())
                        .thenComparing(Menu::getMenuId))
                .map(menu -> toResult(menu, getCategory(categories, menu)))
                .toList();
    }

    private MenuCategory getCategory(Map<Long, MenuCategory> categories, Menu menu) {
        MenuCategory category = categories.get(menu.getCategoryId());

        if (category == null) {
            throw new BusinessException(ErrorCode.MENU_CATEGORY_MISSING);
        }

        return category;
    }

    private MenuResult toResult(Menu menu, MenuCategory category) {
        return new MenuResult(
                menu.getMenuId(),
                category.getCategoryId(),
                category.getName(),
                menu.getName(),
                menu.getPrice()
        );
    }
}
