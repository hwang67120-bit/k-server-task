package com.example.kservertask.menu.repository;

import com.example.kservertask.menu.entity.MenuOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuOptionRepository extends JpaRepository<MenuOption, Long> {

    List<MenuOption> findAllByMenuIdOrderByOptionIdAsc(Long menuId);
}
