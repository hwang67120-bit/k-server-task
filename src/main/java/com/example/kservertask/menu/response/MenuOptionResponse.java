package com.example.kservertask.menu.response;

import com.example.kservertask.menu.entity.MenuSize;
import com.example.kservertask.menu.entity.MenuTemperature;
import com.example.kservertask.menu.result.MenuOptionResult;

public record MenuOptionResponse(
        Long optionId,
        MenuSize size,
        MenuTemperature temperature,
        String beanType,
        long additionalPrice,
        boolean syrupAvailable
) {

    public static MenuOptionResponse from(MenuOptionResult result) {
        return new MenuOptionResponse(
                result.optionId(),
                result.size(),
                result.temperature(),
                result.beanType(),
                result.additionalPrice(),
                result.syrupAvailable()
        );
    }
}
