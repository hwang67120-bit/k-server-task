package com.example.kservertask.menu.controller;

import com.example.kservertask.menu.response.MenuListResponse;
import com.example.kservertask.menu.response.MenuResponse;
import com.example.kservertask.menu.result.MenuResult;
import com.example.kservertask.menu.service.MenuService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MenuControllerTest {

    @Test
    void convertsServiceResultToHttpResponse() {
        MenuService menuService = mock(MenuService.class);
        MenuController controller = new MenuController(menuService);
        when(menuService.getMenus()).thenReturn(List.of(
                new MenuResult(1L, 1L, "Coffee", "Americano", 4500)
        ));

        MenuListResponse response = controller.getMenus();

        assertThat(response.menus()).containsExactly(
                new MenuResponse(1L, 1L, "Coffee", "Americano", 4500)
        );
    }
}
