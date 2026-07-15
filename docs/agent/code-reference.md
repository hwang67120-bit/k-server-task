# 코드 기준

## 기준 우선순위

다음 순서로 코드 기준을 선택한다.

1. 사용자가 검토하고 승인한 현재 프로젝트 코드와 테스트
2. 선택한 Spring Boot 세대와 맞는 Spring 공식 실행 예제
3. Spring 공식 문서와 기술 명세
4. 목적과 신뢰성을 설명하고 승인받은 다른 자료

처음 승인된 메뉴 구현을 Controller, Service, Repository, Response와 테스트 형태의 프로젝트 내부 기준으로 삼는다. 다른 도메인은 비즈니스 동작 때문에 차이가 필요한 경우가 아니라면 같은 형태를 따른다.

## 공식 실행 예제

- Spring Petclinic: https://github.com/spring-projects/spring-petclinic
- REST API: https://spring.io/guides/gs/rest-service
- Spring Data JPA: https://spring.io/guides/gs/accessing-data-jpa
- 트랜잭션: https://spring.io/guides/gs/managing-transactions
- 캐시: https://spring.io/guides/gs/caching
- Spring Boot 테스트: https://docs.spring.io/spring-boot/reference/testing/index.html
- Spring Boot Testcontainers: https://docs.spring.io/spring-boot/reference/testing/testcontainers.html
- Spring Kafka와 샘플: https://github.com/spring-projects/spring-kafka

이 자료들은 코드 형태의 참고 기준이다. 자료에 포함된 모든 기능과 어노테이션을 프로젝트에 적용해도 된다는 뜻이 아니다.

## 형태 예시

아래 코드는 이름과 책임의 형태만 보여준다. 프로젝트에서 검토되기 전에는 승인된 구현이 아니다.

```java
@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public MenuListResponse getMenus() {
        return menuService.getMenus();
    }
}
```

- Controller는 HTTP 요청을 받고 Service에 작업을 위임한다.
- Controller에서 Repository를 직접 호출하거나 비즈니스 정책을 처리하지 않는다.
- 검증된 필요가 없다면 Null 안정성 어노테이션이나 선택적인 프레임워크 어노테이션을 추가하지 않는다.

## 이름 예시

우선 사용하는 이름:

```text
MenuController
PointService
OrderRepository
getMenus
addPoint
createOrder
sendOrderEvent
```

명확한 이유 없이 피할 이름:

```text
CommonUtil
DataManager
OrderProcessor
process
handleData
```

## 로그 예시

장애 흐름을 복원할 수 없는 로그는 피한다.

```java
log.info("service started");
log.info("database query finished");
```

대상 식별값, 결과와 재시도 문맥을 기록한다.

```java
log.info("Order payment completed. orderId={}, amount={}", orderId, amount);
log.warn("Order event retry scheduled. eventId={}, attempt={}", eventId, attempt);
log.error("Order event delivery failed. eventId={}", eventId, exception);
```

예외 스택은 예외를 최종 처리하는 경계에서 한 번 기록한다. 비밀값과 개인정보는 기록하지 않는다.

## 패턴 적용 확인

외부 패턴을 가져오기 전에 다음 질문에 답한다.

- 승인된 요구사항을 해결하는가?
- 선택한 Java와 Spring Boot 버전에 맞는가?
- 더 단순한 프로젝트 내부 방식으로 충분하지 않은가?
- 사용자가 존재 이유를 설명할 수 있는가?
- 효과를 검증할 수 있는가?
- 사용자가 적용을 승인했는가?
