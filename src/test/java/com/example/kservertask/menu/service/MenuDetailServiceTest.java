package com.example.kservertask.menu.service;

import com.example.kservertask.error.BusinessException;
import com.example.kservertask.error.ErrorCode;
import com.example.kservertask.menu.entity.Menu;
import com.example.kservertask.menu.entity.MenuCategory;
import com.example.kservertask.menu.entity.MenuOption;
import com.example.kservertask.menu.entity.MenuSize;
import com.example.kservertask.menu.entity.MenuTemperature;
import com.example.kservertask.menu.repository.MenuCategoryRepository;
import com.example.kservertask.menu.repository.MenuOptionRepository;
import com.example.kservertask.menu.repository.MenuRepository;
import com.example.kservertask.menu.result.MenuDetailResult;
import com.example.kservertask.menu.result.MenuOptionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuDetailServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuCategoryRepository menuCategoryRepository;

    @Mock
    private MenuOptionRepository menuOptionRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    void returnsMenuDetailWithOptions() {
        Menu menu = menu(1L, 1L, "Americano", 4500);
        MenuCategory category = category(1L, "Coffee", 1);
        MenuOption regular = option(1L, 1L, MenuSize.REGULAR, MenuTemperature.HOT, 0);
        MenuOption large = option(2L, 1L, MenuSize.LARGE, MenuTemperature.ICE, 500);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(menuCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(menuOptionRepository.findAllByMenuIdOrderByOptionIdAsc(1L))
                .thenReturn(List.of(regular, large));

        MenuDetailResult result = menuService.getMenu(1L);

        assertThat(result).isEqualTo(new MenuDetailResult(
                1L,
                1L,
                "Coffee",
                "Americano",
                4500,
                List.of(
                        new MenuOptionResult(
                                1L,
                                MenuSize.REGULAR,
                                MenuTemperature.HOT,
                                "Dark Roast",
                                0,
                                true
                        ),
                        new MenuOptionResult(
                                2L,
                                MenuSize.LARGE,
                                MenuTemperature.ICE,
                                "Dark Roast",
                                500,
                                true
                        )
                )
        ));
    }

    @Test
    void returnsEmptyOptionsWhenMenuHasNoOption() {
        Menu menu = menu(3L, 2L, "Cheese Cake", 6000);
        MenuCategory category = category(2L, "Cake", 2);

        when(menuRepository.findById(3L)).thenReturn(Optional.of(menu));
        when(menuCategoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(menuOptionRepository.findAllByMenuIdOrderByOptionIdAsc(3L)).thenReturn(List.of());

        MenuDetailResult result = menuService.getMenu(3L);

        assertThat(result.options()).isEmpty();
    }

    @Test
    void rejectsMissingMenu() {
        when(menuRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.getMenu(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.MENU_NOT_FOUND.getMessage());
        verifyNoInteractions(menuCategoryRepository, menuOptionRepository);
    }

    @Test
    void rejectsMenuWhenCategoryIsMissing() {
        Menu menu = menu(1L, 1L, "Americano", 4500);
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(menuCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.getMenu(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.MENU_CATEGORY_MISSING.getMessage());
        verifyNoInteractions(menuOptionRepository);
    }

    private Menu menu(Long menuId, Long categoryId, String name, long price) {
        Menu menu = new Menu(categoryId, name, price);
        ReflectionTestUtils.setField(menu, "menuId", menuId);
        return menu;
    }

    private MenuCategory category(Long categoryId, String name, int displayOrder) {
        MenuCategory category = new MenuCategory(name, displayOrder);
        ReflectionTestUtils.setField(category, "categoryId", categoryId);
        return category;
    }

    private MenuOption option(
            Long optionId,
            Long menuId,
            MenuSize size,
            MenuTemperature temperature,
            long additionalPrice
    ) {
        MenuOption option = new MenuOption(
                menuId,
                size,
                temperature,
                "Dark Roast",
                additionalPrice,
                true
        );
        ReflectionTestUtils.setField(option, "optionId", optionId);
        return option;
    }
}
