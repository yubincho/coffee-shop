# Coffee Order Service ![image](https://github.com/user-attachments/assets/573dddcd-4c75-44ef-a24f-f877c301c2cc)


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
- ERD
![쇼핑몰ERD](https://github.com/user-attachments/assets/9fc16349-6f87-418d-b82c-03d3c4b98c63)
- Api Endpoints
  
![image](https://github.com/user-attachments/assets/8c577ae5-0377-494a-a278-565c92a8e454)

   ( Postman Docs : https://documenter.getpostman.com/view/11038161/2sAXqzWy27 )

## 기술 스택

- Backend: Spring Boot
- Database: MySQL
- ORM: JPA (Java Persistence API)
- Authentication: JWT, OAuth2
- 결제 서비스: 아임포트 (테스트용)
- 성능 테스트 : K6, Grafana (업데이트 중)
- QueryDSL
- Postman, Swagger
- Docker
- Kafka

추가 구현중인 기술:
- 스프링 배치
- AWS 클라우드


## 설치 및 실행 방법

- java : 17, intelliJ, Spring Boot : 3.2.10

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

![그라파나1](https://github.com/user-attachments/assets/ac3150e7-777e-42de-9451-e9760488c0ff)


이러한 성능 최적화 기법을 통해 서비스의 응답 시간을 개선하고, 대규모 트래픽 처리 능력을 향상시켰습니다.

## 프로젝트 회고 및 향후 계획

### 1. 기술적 도전과 극복

이 프로젝트를 통해 다음과 같은 기술적 도전을 경험하고 극복했습니다:

- **JWT 인증 시스템 구현**: 보안성 높은 사용자 인증 시스템을 구축하며 토큰 기반 인증의 이해도를 높였습니다.
- **성능 최적화**: 커서 기반 페이징, 응답 처리 최적화 등을 통해 대규모 데이터 처리 능력을 향상시켰습니다.
- **결제 시스템 연동**: 아임포트 API를 활용하여 실제 결제 프로세스를 구현하며 외부 API 연동 경험을 쌓았습니다.

### 2. 배운 점

- **테스트 주도 개발(TDD)의 중요성**: 단위 테스트를 작성중이며 코드의 안정성과 유지보수성을 높이는 방법을 학습했습니다.
- **성능 모니터링의 가치**: K6와 Grafana를 활용한 부하 테스트를 통해 성능 병목 지점을 파악하고 개선하는 과정의 중요성을 깨달았습니다.
- **확장성 있는 설계의 필요성**: 시스템 구조를 설계할 때 미래의 확장성을 고려하는 것의 중요성을 인식했습니다.

### 3. 향후 개선 계획

- **마이크로서비스 아키텍처 도입**: 현재의 모놀리식 구조를 마이크로서비스로 전환하여 각 기능의 독립적인 확장과 관리를 가능하게 할 계획입니다.
- **알림 기능**: Kafka와 배치를 활용하여 알림 시스템을 구현할 예정입니다.
- **클라우드 네이티브 전환**: AWS 서비스를 활용하여 클라우드 네이티브 애플리케이션으로 전환할 계획입니다.
- **사용자 경험 개선**: 프론트엔드 프레임워크를 도입하여 더 나은 사용자 경험을 제공하고자 합니다.

### 4. 프로젝트 기여 방법

이 프로젝트에 기여하고 싶으신 분들은 다음과 같은 방법으로 참여하실 수 있습니다:

1. 이슈 트래커에서 해결되지 않은 이슈를 확인하고 작업해 주세요.
2. 새로운 기능 제안이나 버그 리포트는 이슈를 생성해 주세요.
3. 코드 개선이나 새 기능 구현은 풀 리퀘스트를 통해 제출해 주세요.
4. 코드 리뷰에 참여하여 다른 개발자들의 코드에 피드백을 제공해 주세요.


### 5. 연락처 및 포트폴리오

더 자세한 정보나 협업 기회를 위해 연락 주세요:

- 이메일: yubinch9@gmail.com
- 개인 블로그: (https://www.notion.so/d981b86f1fd74fe685bc9889a9b1b7c9)

이 프로젝트를 포함한 다른 작업물은 제 [GitHub 프로필]에서 확인하실 수 있습니다.
