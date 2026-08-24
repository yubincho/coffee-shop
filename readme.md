# Coffee Order Service

커피 주문 서비스 프로젝트입니다. 온라인으로 커피를 주문하고 결제하는 기능을 제공하며, 인증·상품·장바구니·주문 도메인으로 구성되어 있습니다.

## 목차

- [프로젝트 개요](#프로젝트-개요)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [설치 및 실행 방법](#설치-및-실행-방법)
- [문제 해결 기록](#문제-해결-기록)
- [향후 계획](#향후-계획)
- [연락처](#연락처)

## 프로젝트 개요

사용자가 온라인으로 커피를 주문할 수 있는 서비스입니다. JWT 기반 인증과 Google OAuth2 로그인을 지원하며, 상품 관리·장바구니·주문·결제 흐름을 갖추고 있습니다. 

## 주요 기능

- 사용자 인증 (JWT 기반, Access / Refresh Token)
- Google OAuth2 로그인
- 상품 관리 (CRUD, 커서 기반 페이징 검색)
- 장바구니 담기 / 수량 변경
- 커피 주문 및 결제 (아임포트 테스트 연동)
- Kafka 기반 사용자 활동 이벤트 처리 및 추천 데이터 생성

## 기술 스택

| 구분 | 사용 기술                       |
| --- |-----------------------------|
| Backend | Spring Boot 3.2.10, Java 17 |
| Database | MySQL, Redis                      |
| ORM | JPA (Hibernate), QueryDSL   |
| 인증 | JWT, OAuth2                 |
| 결제 | 아임포트 (테스트용)                 |
| 메시징 | Kafka                       |
| 캐시 / 분산 락 | Redis, Redisson |
| 성능 테스트 | k6 (부하 스크립트)  , Grafana              |
| 문서화 | Swagger, Postman            |
| 인프라 | Docker                      |

## 프로젝트 구조

도메인형 패키지 구조로 구성되어 있습니다. 인증·상품·장바구니·주문·유저 등 도메인 단위로 `controller`, `service`, `repository`, `entity`, `dto`를 묶어, 기능별 응집도를 높이고 계층 간 의존성을 명확히 했습니다.

## 설치 및 실행 방법

- Java 17, Spring Boot 3.2.10
- MySQL 실행 후 `application.yml`에 DB 접속 정보 설정
- Redis 실행 (`docker-compose up`으로 Redis 컨테이너 실행) — 캐시·분산 락에 사용
- (선택) Kafka 사용 시 `docker-compose up`으로 Kafka / Zookeeper 실행
- `./gradlew bootRun` 으로 애플리케이션 실행
- API 문서: `http://localhost:8080/swagger-ui/index.html`

## 문제 해결 기록

### 1. 순환참조 해결 (JwtProvider ↔ UserService)

**문제**: `JwtProvider`가 Access Token 생성 시 `Authentication`에서 이메일을 꺼내 `UserService`로 User를 재조회하면서, `JwtProvider`와 `UserService` 사이에 순환참조가 발생했다. 임시로 `@Lazy`를 붙여 막아둔 상태였다.

**해결**: 토큰 생성 메서드가 `Authentication` 대신 이미 조회된 `User`를 직접 받도록 변경했다. 이로써 로그인 시 발생하던 User 이중 조회를 제거하고, `JwtProvider`의 `UserService` 의존을 `UserRepository`로 대체해 `@Lazy` 없이 순환참조를 근본적으로 제거했다.

### 2. products/all 조회 500 에러 (LazyInitializationException & N+1)

**문제**: 전체 상품 조회 시 `LazyInitializationException`으로 500 에러가 발생했다. `Product.images`가 지연 로딩인데, 트랜잭션이 끝난 뒤 DTO 변환 과정에서 컬렉션에 접근하면서 프록시 초기화에 실패한 것이다.

**해결**: QueryDSL 조회를 두 단계로 나눴다. 먼저 커서 페이징으로 상품 ID만 조회하고(limit 정확성 보장), 그 ID들로 `images`를 fetch join 해서 한 번에 가져오도록 했다. 컬렉션 fetch join과 페이징을 함께 쓸 때 발생하는 limit 무효화(`HHH000104`)를 피하기 위한 패턴이다. 결과적으로 상품 3개 기준 쿼리가 5회에서 3회로 줄며 N+1이 제거되었다.

### 3. 트랜잭션 관련 오류 (로그인 / 로그아웃)

**문제**: JPA 삭제 작업 시 `TransactionRequiredException` 발생.

**해결**: DB 변경이 있는 로그인·로그아웃 메서드에 `@Transactional` 추가.

```java
@Transactional
public String login(LoginRequest loginRequest) {
    // 로직
}
```

### 4. Product 업데이트 시 새 객체 생성 문제

**문제**: 상품 수정 시 기존 객체가 아닌 새 객체가 생성됨.

**해결**: `findById`로 기존 엔티티를 조회한 뒤 필드를 변경하는 방식(dirty checking)으로 수정.

```java
public Product updateProduct(long id, UpdateProductRequest request) {
    return productRepository.findById(id)
            .map(existingProduct -> {
                existingProduct.setName(request.getName());
                // 다른 필드도 동일하게 변경
                return productRepository.save(existingProduct);
            })
            .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
}
```

### 5. 재고 동시성 제어 

**문제**: 인기 상품에 주문이 동시에 몰릴 때, 여러 요청이 같은 재고 값을 읽고 각자 차감해 저장하면서 갱신 손실(lost update)이 발생했다. 재고 100개에 100건의 동시 주문을 보내는 테스트에서, 최종 재고가 0이 아닌 90 부근으로 남아 약 90%의 차감이 유실되는 것을 확인했다.

**비관적 락 vs 낙관적 락 vs Redisson 분산 락 성능 비교**: 세 전략을 모두 구현해 동일 조건(재고 100, 동시 100요청)에서 측정한 결과는 다음과 같다.

| 락 전략 | 평균 소요 시간 |
| --- | --- |
| 비관적 락 | 약 2,300ms |
| 낙관적 락 | 약 6,500ms |
| Redisson 분산 락 | 약 4,500ms |

충돌이 잦은 재고 차감 상황에서는 재시도 비용이 없는 비관적 락이 가장 빨랐다. Redisson 분산 락은 매 요청마다 Redis 왕복(락 획득·해제)이 추가되어 단일 서버·극한 경합 조건에서는 비관적 락보다 느렸다. 다만 Redisson의 이점은 이 벤치마크가 측정하지 못하는 부분, 즉 락 대기 중 DB 커넥션을 점유하지 않고 여러 서버로 확장해도 일관되게 동작한다는 데 있다. 따라서 단일 서버에서는 비관적 락으로 충분하고, DB 커넥션 경합이 병목이 되거나 수평 확장하는 시점에 Redisson이 의미를 갖는다.


### 6. 성능 최적화

- **커서 기반 페이징**: 오프셋 방식 대신 커서(`id > ?`) 기반 페이징을 적용해 대용량 데이터 조회 성능을 개선. 다음 페이지 존재 여부는 불필요한 count 쿼리 대신 `limit + 1` 방식으로 판단하도록 정리했다.
- **응답 처리 최적화**: 모든 응답에 mapper를 적용하는 대신, 필요한 경우에만 DTO로 변환.
- **Redis 캐시 적용**: 상품 목록 조회(`GET /products/all`)에 Redis 캐시를 적용했다. `cursor + size + keyword` 조합을 캐시 키로 사용하고, 상품 변경 시 목록 캐시를 무효화(`@CacheEvict`)한다. 캐시 값은 엔티티가 아닌 DTO(`PageResponseDto`)를 저장해 지연 로딩·직렬화 문제를 피했다.
- **부하 테스트 (조회 API)**: 상품 100,003건 · 300 VU · 5분 조건에서 k6로 측정했다.

  | 구간 | p95 응답 시간 | 처리량(TPS) | 실패율 | 500ms 통과율 |
    | --- | --- | --- | --- | --- |
  | count 쿼리 제거 후 (캐시 전) | 약 2.79s | 약 125 | 0% | 47% |
  | 캐시 적용 (첫 페이지 반복 조회) | 약 25.89ms | 약 236 | 0% | 99% |
  | 캐시 적용 (실제 패턴: 랜덤 커서 + 키워드) | 약 3.87s | 약 86 | 0% | - |

   첫 페이지 반복 조회에서는 캐시 히트율이 사실상 100%에 가까워 p95가 약 108배(2.79s → 25.89ms) 개선됐고, 오랫동안 넘지 못하던 p95 < 500ms 목표를 처음으로 달성했다. 다만 이는 캐시가 낼 수 있는 성능의 상한선이다. 랜덤 커서와 키워드 검색이 섞인 실제 사용 패턴에서는 캐시 키가 흩어져 히트율이 급락하고, `LIKE '%keyword%'` 풀스캔이 다수 발생해 p95가 오히려 3.87s로 올라갔다(처리량도 236 → 86 TPS로 하락). 즉 캐시는 반복 조회에는 강하지만 다양한 조회·검색이 섞이면 한계가 있으며, 검색 병목은 캐시로도 인덱스로도 해결되지 않아 향후 Elasticsearch로 분리할 계획이다.
- **주문 API 부하 테스트의 한계**: 주문 API(`/api/v1/orders/.../place-order`)는 주문 성공 시 장바구니를 초기화(`clearCart`)하는 구조여서, 동일 요청을 반복하는 HTTP 부하 테스트에는 부적합하다. 그래서 주문 쪽 동시성은 JUnit 기반 동시성 테스트(스레드 100개, 재고 정합성 + 소요 시간 측정)로 검증했다.


## 향후 계획

- **검색 엔진 도입**: 상품 검색의 `LIKE '%keyword%'`는 선행 와일드카드 때문에 B-tree 인덱스를 타지 못하고 풀스캔이 발생한다. 검색 품질(부분 일치)을 유지하면서 성능을 확보하기 위해 Elasticsearch(한글 n-gram) 도입을 검토 중이다.
- **테스트 보강**: `AuthControllerTest`의 JWT 예외 처리 테스트 수정.
- **결제 흐름 완성**: 아임포트 결제 확정(`/orders/done`) 연동 마무리.

## 연락처

- 이메일: yubinch9@gmail.com
- 블로그: (https://itsgoood.tistory.com/)
