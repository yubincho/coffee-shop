# Coffee Order Service

커피 주문 서비스 프로젝트입니다.

## 목차
- [프로젝트 개요](#프로젝트-개요)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [설치 및 실행 방법](#설치-및-실행-방법)
- [문제 해결 기록](#문제-해결-기록)
    - [순환 참조 오류](#순환-참조-오류)
    - [트랜잭션 관련 오류](#트랜잭션-관련-오류)
    - [JWT 인증 시스템](#jwt-인증-시스템)
    - [Google OAuth2 로그인](#google-oauth2-로그인)
    - [Product 업데이트 문제](#product-업데이트-문제)
    - [성능 최적화](#성능-최적화)
- [프로젝트 회고 및 향후 계획](#프로젝트 회고 및 향후 계획)

## 프로젝트 개요

이 프로젝트는 사용자가 온라인으로 커피를 주문할 수 있는 서비스를 제공합니다.

## 주요 기능

- 사용자 인증 (JWT 기반)
- Google OAuth2 로그인
- 커피 주문 및 결제
- 상품 관리 (CRUD 작업)

## 기술 스택

- Backend: Spring Boot
- Database: MySQL
- ORM: JPA (Java Persistence API)
- Authentication: JWT, OAuth2
- 결제 서비스: 아임포트 (테스트용)
- 성능 테스트 : K6, Grafana (업데이트 중)
- Postman, Swagger

추가 구현중인 기술:
- 스프링 배치
- Kafka
- AWS 클라우드
- Docker 

## 설치 및 실행 방법

[프로젝트 설치 및 실행 방법에 대한 설명]

## 문제 해결 기록

### 순환 참조 오류

#### 문제 설명
`CoffeeShopConfig`, `JwtProvider`, `UserService` 사이의 순환 참조 문제가 발생했습니다.

#### 해결 방법
1. `@Lazy` 어노테이션 사용
   ```java
   @Lazy
   private final JwtProvider jwtProvider;
   ```

2. Setter 주입 사용
   ```java
   @Autowired
   public void setUserService(@Lazy UserService userService) {
       this.userService = userService;
   }
   ```

3. 의존성 분리 (프로젝트가 복잡해질 수 있음)

### 트랜잭션 관련 오류

#### 문제 설명
JPA 삭제 작업 시 `TransactionRequiredException` 발생

#### 해결 방법
관련 메서드에 `@Transactional` 어노테이션 추가
```java
@Transactional
public String login(LoginRequest loginRequest) {
    // 로직
}

@Transactional
public void logout(LogOutRequest logoutRequest) {
    // 로직
}
```

### JWT 인증 시스템

#### 전체 흐름
1. 회원가입/로그인
2. JWT 생성
3. 사용자 요청 시 `AuthTokenFilter`에서 JWT 검증
4. 검증 통과 시 사용자 인증 완료
5. 예외 발생 시 `JwtAuthEntryPoint`에서 401 응답 처리

#### 로그인 로직
1. 리프레시 토큰을 데이터베이스에 저장
2. 리프레시 토큰 검증
3. 리프레시 토큰 만료 시 자동 로그아웃

### Google OAuth2 로그인

접근 URL: `http://localhost:8080/oauth2/authorization/google`

### Product 업데이트 문제

#### 문제 설명
Product 업데이트 시 새로운 객체가 생성되는 문제 발생

#### 해결 방법
기존 객체를 수정하도록 로직 변경
```java
public Product updateProduct(long id, UpdateProductRequest request) {
    return productRepository.findById(id)
            .map(existingProduct -> {
                existingProduct.setName(request.getName());
                // 다른 필드들도 유사하게 설정
                return productRepository.save(existingProduct);
            })
            .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
}
```

### 성능 최적화

#### 페이징 처리 개선
커서 기반 페이징을 적용하여 대용량 데이터 처리 시 성능을 향상시켰습니다.

```java
public List<Product> getProductsAfter(Long cursorId, int limit) {
    return productRepository.findProductsAfter(cursorId, PageRequest.of(0, limit));
}
```

#### 응답 처리 최적화
모든 응답에 mapper를 적용하는 대신, 필요한 경우에만 DTO를 사용하여 응답 처리 시간을 단축했습니다.

```java
public ProductDTO getProduct(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    return new ProductDTO(product);  // 필요한 경우에만 DTO 변환
}
```

#### 부하 테스트 도구 적용
- K6: 부하 테스트 스크립트 작성 및 실행
- Grafana: 테스트 결과 시각화 및 분석

```javascript
import http from 'k6/http';
import { sleep } from 'k6';

export default function () {
  http.get('http://localhost:8080/api/products');
  sleep(1);
}
```

이러한 성능 최적화 기법을 통해 서비스의 응답 시간을 개선하고, 대규모 트래픽 처리 능력을 향상시켰습니다.

