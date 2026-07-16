# 디렉터리 구조

프로젝트는 도메인 기준 구조를 사용한다. 이름이나 형식 때문에 다른 아키텍처를 도입하지 않는다. 현재 구조로 명확하게 해결할 수 없는 의존성 또는 변경 문제가 실제로 확인될 때 재검토한다.

```text
src/main/java/.../kservertask/
|-- menu/
|   |-- controller/
|   |-- service/
|   |-- repository/
|   |-- entity/
|   |-- request/
|   |-- result/
|   `-- response/
|-- point/
|   |-- controller/
|   |-- service/
|   |-- repository/
|   |-- entity/
|   |-- request/
|   `-- response/
|-- order/
|   |-- controller/
|   |-- service/
|   |-- repository/
|   |-- entity/
|   |-- request/
|   `-- response/
|-- event/
|   |-- producer/
|   |-- consumer/
|   |-- repository/
|   `-- entity/
|-- base/
|   `-- entity/
|-- config/
`-- error/

src/main/resources/
|-- application.yml
`-- db/migration/

src/test/java/.../kservertask/
|-- menu/
|-- point/
|-- order/
`-- event/
```

## 규칙

- 실제 클래스가 생겼을 때만 해당 책임의 하위 패키지를 만든다.
- 테스트는 검증 대상과 같은 도메인 아래에 둔다.
- `common` 또는 `util` 패키지를 미리 만들지 않는다.
- 실제 중복과 데이터 소유권이 확인된 뒤에만 공통 코드를 분리한다.
- 여러 도메인과 관련된 기능은 데이터 소유권과 의존 방향을 분석하고, 합의한 구조를 바꾼다면 사용자 승인을 받는다.
