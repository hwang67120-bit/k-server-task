package com.example.kservertask.menu.service;

import com.example.kservertask.error.BusinessException;
import com.example.kservertask.error.ErrorCode;

import com.example.kservertask.menu.entity.Menu;
import com.example.kservertask.menu.entity.MenuCategory;
import com.example.kservertask.menu.entity.MenuOption;
import com.example.kservertask.menu.repository.MenuCategoryRepository;
import com.example.kservertask.menu.repository.MenuOptionRepository;
import com.example.kservertask.menu.repository.MenuRepository;
import com.example.kservertask.menu.result.MenuDetailResult;
import com.example.kservertask.menu.result.MenuOptionResult;
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
    private final MenuOptionRepository menuOptionRepository;

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

    public MenuDetailResult getMenu(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
        MenuCategory category = menuCategoryRepository.findById(menu.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_CATEGORY_MISSING));
        List<MenuOptionResult> options = menuOptionRepository
                .findAllByMenuIdOrderByOptionIdAsc(menuId)
                .stream()
                .map(this::toOptionResult)
                .toList();

        return new MenuDetailResult(
                menu.getMenuId(),
                category.getCategoryId(),
                category.getName(),
                menu.getName(),
                menu.getPrice(),
                options
        );
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

    private MenuOptionResult toOptionResult(MenuOption option) {
        return new MenuOptionResult(
                option.getOptionId(),
                option.getSize(),
                option.getTemperature(),
                option.getBeanType(),
                option.getAdditionalPrice(),
                option.isSyrupAvailable()
        );
    }
}
