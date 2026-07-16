package com.example.kservertask.menu.entity;

import com.example.kservertask.base.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "menu_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public MenuCategory(String name, int displayOrder) {
        this.name = name;
        this.displayOrder = displayOrder;
    }
}
