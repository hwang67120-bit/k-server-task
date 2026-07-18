package com.example.kservertask.menu.service;

import com.example.kservertask.error.BusinessException;
import com.example.kservertask.error.ErrorCode;

import com.example.kservertask.menu.entity.Menu;
import com.example.kservertask.menu.entity.MenuCategory;
import com.example.kservertask.menu.entity.MenuOption;
import com.example.kservertask.event.repository.DailyProductSalesRepository;
import com.example.kservertask.event.repository.ProductSalesProjection;
import com.example.kservertask.menu.repository.MenuCategoryRepository;
import com.example.kservertask.menu.repository.MenuOptionRepository;
import com.example.kservertask.menu.repository.MenuRepository;
import com.example.kservertask.menu.result.MenuDetailResult;
import com.example.kservertask.menu.response.PopularMenuListResponse;
import com.example.kservertask.menu.response.PopularMenuResponse;
import com.example.kservertask.menu.result.MenuOptionResult;
import com.example.kservertask.menu.result.MenuResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
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
    private final DailyProductSalesRepository dailyProductSalesRepository;

    /**
     * 기능: 전체 메뉴를 카테고리 표시 순서와 메뉴 ID 순서로 조회한다.
     *
     * 파라미터:
     * - 없음
     *
     * 요청값:
     * - 없음
     *
     * 응답값:
     * - List<MenuResult>: 메뉴 ID, 카테고리 ID, 카테고리명, 메뉴명, 가격
     */
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

    /**
     * 기능: 메뉴 ID로 메뉴 상세 정보와 옵션을 조회한다.
     *
     * 파라미터:
     * - menuId: 조회할 메뉴 ID
     *
     * 요청값:
     * - menuId: 메뉴를 식별하는 값
     *
     * 응답값:
     * - MenuDetailResult: 메뉴 ID, 카테고리 정보, 메뉴명, 가격, 옵션 목록
     */
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

    /**
     * 기능: 메뉴에 연결된 카테고리를 조회한다. (내부 처리)
     *
     * 파라미터:
     * - categories: 카테고리 ID와 카테고리 정보의 목록
     * - menu: 카테고리 ID를 가진 메뉴
     *
     * 요청값:
     * - categoryId: 메뉴에 저장된 카테고리 식별값
     *
     * 응답값:
     * - MenuCategory: 메뉴에 연결된 카테고리 정보
     */
    /**
     * 기능: 최근 7일간 주문 횟수가 5회 이상인 인기 메뉴 상위 3개를 조회한다.
     *
     * 파라미터:
     * - 없음
     *
     * 요청값:
     * - 없음
     *
     * 응답값:
     * - PopularMenuListResponse: 조회 기간과 인기 메뉴 목록
     */
    public PopularMenuListResponse getPopularMenus() {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        LocalDate toDate = LocalDate.now(zoneId);
        LocalDate fromDate = toDate.minusDays(7);

        List<ProductSalesProjection> sales = dailyProductSalesRepository
                .findTopPopularMenus(fromDate, 5, PageRequest.of(0, 3));

        Map<Long, Menu> menus = new LinkedHashMap<>();
        menuRepository.findAllById(sales.stream()
                        .map(ProductSalesProjection::getProductId)
                        .toList())
                .forEach(menu -> menus.put(menu.getMenuId(), menu));

        List<PopularMenuResponse> responses = new java.util.ArrayList<>();
        for (int index = 0; index < sales.size(); index++) {
            ProductSalesProjection sale = sales.get(index);
            Menu menu = menus.get(sale.getProductId());

            if (menu == null) {
                throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
            }

            responses.add(new PopularMenuResponse(
                    index + 1,
                    menu.getMenuId(),
                    menu.getName(),
                    sale.getTotalOrderCount()
            ));
        }

        return new PopularMenuListResponse(
                fromDate.atStartOfDay(zoneId).toOffsetDateTime(),
                toDate.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime(),
                responses
        );
    }

    private MenuCategory getCategory(Map<Long, MenuCategory> categories, Menu menu) {
        MenuCategory category = categories.get(menu.getCategoryId());

        if (category == null) {
            throw new BusinessException(ErrorCode.MENU_CATEGORY_MISSING);
        }

        return category;
    }

    /**
     * 기능: 메뉴와 카테고리 정보를 메뉴 목록 응답으로 변환한다. (내부 처리)
     *
     * 파라미터:
     * - menu: 메뉴 엔티티
     * - category: 메뉴에 연결된 카테고리 엔티티
     *
     * 요청값:
     * - menuId, categoryId, name, price: 응답으로 변환할 메뉴 정보
     *
     * 응답값:
     * - MenuResult: 메뉴 목록용 응답 정보
     */
    private MenuResult toResult(Menu menu, MenuCategory category) {
        return new MenuResult(
                menu.getMenuId(),
                category.getCategoryId(),
                category.getName(),
                menu.getName(),
                menu.getPrice()
        );
    }

    /**
     * 기능: 메뉴 옵션 엔티티를 옵션 응답으로 변환한다. (내부 처리)
     *
     * 파라미터:
     * - option: 메뉴 옵션 엔티티
     *
     * 요청값:
     * - optionId, size, temperature, beanType, additionalPrice, syrupAvailable: 옵션 정보
     *
     * 응답값:
     * - MenuOptionResult: 메뉴 상세 조회용 옵션 정보
     */
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
