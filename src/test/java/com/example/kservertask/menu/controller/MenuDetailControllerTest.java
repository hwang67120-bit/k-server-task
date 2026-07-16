package com.example.kservertask.menu.controller;

import com.example.kservertask.menu.entity.MenuSize;
import com.example.kservertask.menu.entity.MenuTemperature;
import com.example.kservertask.menu.response.MenuDetailResponse;
import com.example.kservertask.menu.response.MenuOptionResponse;
import com.example.kservertask.menu.result.MenuDetailResult;
import com.example.kservertask.menu.result.MenuOptionResult;
import com.example.kservertask.menu.service.MenuService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuDetailControllerTest {

    @Mock
    private MenuService menuService;

    @InjectMocks
    private MenuController menuController;

    @Test
    void convertsMenuDetailResultToResponse() {
        MenuDetailResult result = new MenuDetailResult(
                1L,
                1L,
                "Coffee",
                "Americano",
                4500,
                List.of(new MenuOptionResult(
                        1L,
                        MenuSize.REGULAR,
                        MenuTemperature.HOT,
                        "Dark Roast",
                        0,
                        true
                ))
        );
        when(menuService.getMenu(1L)).thenReturn(result);

        MenuDetailResponse response = menuController.getMenu(1L);

        assertThat(response).isEqualTo(new MenuDetailResponse(
                1L,
                1L,
                "Coffee",
                "Americano",
                4500,
                List.of(new MenuOptionResponse(
                        1L,
                        MenuSize.REGULAR,
                        MenuTemperature.HOT,
                        "Dark Roast",
                        0,
                        true
                ))
        ));
    }
}
