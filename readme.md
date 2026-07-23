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

사용자가 온라인으로 커피를 주문할 수 있는 서비스입니다. JWT 기반 인증과 Google OAuth2 로그인을 지원하며, 상품 관리·장바구니·주문·결제 흐름을 갖추고 있습니다. 학습 목적의 프로젝트로, 실제 서비스에서 마주할 법한 문제(순환참조, N+1, 재고 동시성 등)를 하나씩 해결해 나가는 데 초점을 두고 있습니다.

## 주요 기능

- 사용자 인증 (JWT 기반, Access / Refresh Token)
- Google OAuth2 로그인
- 상품 관리 (CRUD, 커서 기반 페이징 검색)
- 장바구니 담기 / 수량 변경
- 커피 주문 및 결제 (아임포트 테스트 연동)
- Kafka 기반 사용자 활동 이벤트 처리 및 추천 데이터 생성

## 기술 스택

| 구분 | 사용 기술 |
| --- | --- |
| Backend | Spring Boot 3.2.10, Java 17 |
| Database | MySQL |
| ORM | JPA (Hibernate), QueryDSL |
| 인증 | JWT, OAuth2 |
| 결제 | 아임포트 (테스트용) |
| 메시징 | Kafka |
| 성능 테스트 | k6 (부하 스크립트) |
| 문서화 | Swagger, Postman |
| 인프라 | Docker |

## 프로젝트 구조

도메인형 패키지 구조로 구성되어 있습니다. 인증·상품·장바구니·주문·유저 등 도메인 단위로 `controller`, `service`, `repository`, `entity`, `dto`를 묶어, 기능별 응집도를 높이고 계층 간 의존성을 명확히 했습니다.

## 설치 및 실행 방법

- Java 17, Spring Boot 3.2.10
- MySQL 실행 후 `application.yml`에 DB 접속 정보 설정
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

### 5. 재고 동시성 제어 (비관적 락)

**문제**: 인기 상품에 주문이 동시에 몰릴 때, 여러 요청이 같은 재고 값을 읽고 각자 차감해 저장하면서 갱신 손실(lost update)이 발생했다. 재고 100개에 100건의 동시 주문을 보내는 테스트에서, 최종 재고가 0이 아닌 90 부근으로 남아 약 90%의 차감이 유실되는 것을 확인했다.

**해결**: 재고 차감 로직을 엔티티(`Product.removeStock`)로 응집하고 재고 부족 시 `OutOfStockException`을 던지도록 했다. 주문 시에는 `@Lock(PESSIMISTIC_WRITE)`로 상품 행에 비관적 락을 걸어(`findByIdWithPessimisticLock`) 조회하므로, 동시 요청이 순차적으로 처리되어 갱신 손실이 사라진다. 100건 동시 주문 테스트에서 최종 재고가 정확히 0이 되는 것을 확인했다.

**비관적 락 vs 낙관적 락 성능 비교**: 두 전략을 모두 구현해 동일 조건(재고 100, 동시 100요청)에서 측정한 결과는 다음과 같다.

| 락 전략 | 평균 소요 시간 |
| --- | --- |
| 비관적 락 | 약 2,300ms |
| 낙관적 락 | 약 6,500ms |

충돌이 잦은 재고 차감 상황에서는 재시도 비용이 없는 비관적 락이 더 유리하다는 결론을 얻었다.

### 6. 성능 최적화

- **커서 기반 페이징**: 오프셋 방식 대신 커서(`id > ?`) 기반 페이징을 적용해 대용량 데이터 조회 성능을 개선.
- **응답 처리 최적화**: 모든 응답에 mapper를 적용하는 대신, 필요한 경우에만 DTO로 변환.
- **부하 테스트**
  - k6로 부하 테스트를 작성·실행하여 API 응답 추이를 확인
  - 주문 API(`/api/v1/orders/.../place-order`)는 주문 성공 시 장바구니를 초기화(`clearCart`)하는 구조여서, 동일 요청을 반복하는 부하 테스트에는 부적합함을 확인
  - 향후 조회 API 대상으로 부하 테스트를 재설계하여 부하 증가에 따른 응답 시간(p95)·처리량(TPS) 추이를 측정할 예정
   
## 향후 계획

- **Redis 재고 이전**: 트래픽이 더 커질 경우 DB 락 비용을 줄이기 위해 재고를 Redis로 옮기고 원자적 연산(`DECR`) 또는 분산 락(Redisson) 적용 검토.
- **테스트 보강**: `AuthControllerTest`의 JWT 예외 처리 테스트 수정.
- 조회 API 기반 부하 테스트 재설계 및 성능 모니터링 도구(Grafana 등) 연동



## 연락처

- 이메일: yubinch9@gmail.com
- 블로그: (https://itsgoood.tistory.com/)
