package com.example.kservertask.menu.entity;

import com.example.kservertask.base.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "menu_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long optionId;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MenuSize size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MenuTemperature temperature;

    @Column(name = "bean_type", nullable = false, length = 100)
    private String beanType;

    @Column(name = "additional_price", nullable = false)
    private long additionalPrice;

    @Column(name = "syrup_available", nullable = false)
    private boolean syrupAvailable;

    @Version
    @Column(nullable = false)
    private Long version;

    public MenuOption(
            Long menuId,
            MenuSize size,
            MenuTemperature temperature,
            String beanType,
            long additionalPrice,
            boolean syrupAvailable
    ) {
        this.menuId = menuId;
        this.size = size;
        this.temperature = temperature;
        this.beanType = beanType;
        this.additionalPrice = additionalPrice;
        this.syrupAvailable = syrupAvailable;
    }
}
