package com.example.kservertask.menu.service;

import com.example.kservertask.error.BusinessException;
import com.example.kservertask.error.ErrorCode;

import com.example.kservertask.menu.entity.Menu;
import com.example.kservertask.menu.entity.MenuCategory;
import com.example.kservertask.menu.repository.MenuCategoryRepository;
import com.example.kservertask.menu.repository.MenuRepository;
import com.example.kservertask.menu.result.MenuResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuCategoryRepository menuCategoryRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    void returnsMenusOrderedByCategoryAndMenu() {
        MenuCategory coffee = category(1L, "Coffee", 1);
        MenuCategory cake = category(2L, "Cake", 2);
        Menu americano = menu(1L, 1L, "Americano", 4500);
        Menu cheeseCake = menu(2L, 2L, "Cheese Cake", 6000);

        when(menuRepository.findAll()).thenReturn(List.of(cheeseCake, americano));
        when(menuCategoryRepository.findAllById(Set.of(1L, 2L)))
                .thenReturn(List.of(coffee, cake));

        List<MenuResult> results = menuService.getMenus();

        assertThat(results).containsExactly(
                new MenuResult(1L, 1L, "Coffee", "Americano", 4500),
                new MenuResult(2L, 2L, "Cake", "Cheese Cake", 6000)
        );
    }

    @Test
    void rejectsMenuWhenCategoryIsMissing() {
        Menu americano = menu(1L, 1L, "Americano", 4500);
        when(menuRepository.findAll()).thenReturn(List.of(americano));
        when(menuCategoryRepository.findAllById(Set.of(1L))).thenReturn(List.of());

        assertThatThrownBy(menuService::getMenus)
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.MENU_CATEGORY_MISSING.getMessage());
    }

    @Test
    void returnsEmptyListWhenNoMenuExists() {
        when(menuRepository.findAll()).thenReturn(List.of());

        List<MenuResult> results = menuService.getMenus();

        assertThat(results).isEmpty();
        verifyNoInteractions(menuCategoryRepository);
    }

    private MenuCategory category(Long categoryId, String name, int displayOrder) {
        MenuCategory category = new MenuCategory(name, displayOrder);
        ReflectionTestUtils.setField(category, "categoryId", categoryId);
        return category;
    }

    private Menu menu(Long menuId, Long categoryId, String name, long price) {
        Menu menu = new Menu(categoryId, name, price);
        ReflectionTestUtils.setField(menu, "menuId", menuId);
        return menu;
    }
}
