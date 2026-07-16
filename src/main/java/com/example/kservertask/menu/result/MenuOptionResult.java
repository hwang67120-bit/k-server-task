package com.example.kservertask.menu.result;

import com.example.kservertask.menu.entity.MenuSize;
import com.example.kservertask.menu.entity.MenuTemperature;

public record MenuOptionResult(
        Long optionId,
        MenuSize size,
        MenuTemperature temperature,
        String beanType,
        long additionalPrice,
        boolean syrupAvailable
) {
}
