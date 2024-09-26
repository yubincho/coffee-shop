1. 로그인, 페이징 + 검색

2. 부하 테스트 - 결과 문서화
3. 부하 테스트 후 코드 수정 - 캐싱, 커서, 데이터베이스 교체 등
4. 다시 부하 테스트 - 결과 문서화

5. 카프카
6. 배치

7. 배포
 - 도커
 - 젠킨스, CI/CD (깃헙 웹훅)
 - ELK Stack


--------------------------------------------------------------------------------------------------

순환 참조 오류 발생

┌─────┐
|  coffeeShopConfig defined in file [C:\Users\user\Documents\coffeeOrderService\coffeeOrderService\build\classes\java\main\com\example\coffeeOrderService\common\config\CoffeeShopConfig.class]
↑     ↓
|  jwtProvider defined in file [C:\Users\user\Documents\coffeeOrderService\coffeeOrderService\build\classes\java\main\com\example\coffeeOrderService\common\auth\jwt\JwtProvider.class]
↑     ↓
|  userService defined in file [C:\Users\user\Documents\coffeeOrderService\coffeeOrderService\build\classes\java\main\com\example\coffeeOrderService\service\user\UserService.class]
└─────┘


Action:

Relying upon circular references is discouraged and they are prohibited by default.
Update your application to remove the dependency cycle between beans. As a last resort,
it may be possible to break the cycle automatically by setting spring.main.allow-circular-references to true.

[ 문제 해결 ]
순환 참조 문제는 구조적으로 한 쪽에서 의존성 주입 방식을 변경함으로써 해결할 수 있다.
이를 해결하는 일반적인 방법 중 하나는 생성자 주입 대신
@Autowired 또는 @Lazy 사용이나 Setter 주입 방식으로 일부 의존성을 느리게 주입하여 순환을 방지

    @Lazy
    private final JwtProvider jwtProvider;  // @Lazy로 지연 주입

    @Lazy
    private JwtProvider jwtProvider;

    또는

     @Autowired
     public void setUserService(@Lazy UserService userService) {
         this.userService = userService;
     }

     @Autowired
     public void setUserService(UserService userService) {
          this.userService = userService;
     }

결론:
- @Lazy 사용: 순환 참조가 발생하는 의존성에 @Lazy를 추가해 지연 주입을 사용하는 것이 가장 간단한 해결책
- Setter 주입 사용: 순환 참조가 발생하는 빈에 대해 생성자 주입을 Setter 주입 방식으로 변경할 수 있다.
- 의존성 분리: 더 근본적으로 의존성을 재설계하여 결합도를 낮출 수 있다.(프로젝트 복잡해질 수 있음)


--------------------------------------------------------------------------------------------------

jakarta.persistence.TransactionRequiredException:
No EntityManager with actual transaction available for current thread
- cannot reliably process 'remove' call 에러 발생

이 에러는 **TransactionRequiredException**으로, **JPA 삭제 작업 (delete)**을 수행할 때
트랜잭션이 활성화되지 않은 상태에서 작업을 시도했기 때문에 발생한 문제입니다.
JPA는 삭제, 저장 등의 데이터베이스 수정 작업이 트랜잭션 안에서만 이루어져야 하기 때문에, 트랜잭션이 없으면 예외가 발생합니다.

[ 해결 방법 ]
@Transactional 어노테이션을 추가
- 로그인과 로그아웃 메서드에도 추가해줘야 함 ! ***
--> 왜냐면, 로그인, 로그아웃 메서드에 JPA의 데이터 저장, 삭제 등이 이루어지기 때문 !

    @Transactional  //
    public String login(LoginRequest loginRequest) { ...

    @Transactional  // 이 메서드도 트랜잭션 내에서 실행되도록 지정
    private void saveRefreshToken(User user, String refreshToken) { ...

    @Transactional  // 트랜잭션을 적용하여 삭제 작업 처리
    public void logout(LogOutRequest logoutRequest) { ...


---------------------------------------------------------------------------------------------------

[ (JWT 기반) 인증 시스템 전체 흐름 ]
회원가입/로그인 → JWT 생성 → 사용자 요청 시 AuthTokenFilter에서 JWT 검증 → 검증 통과 시 사용자 인증 완료.
예외 발생 시 JwtAuthEntryPoint에서 적절한 401 응답 처리


 [ 로그인 로직 구성 ] - () 내가 아는 범위 내 적합한 방법으로 구성하였음
 1. 리프레시 토큰을 데이터베이스에 저장: 사용자 로그인 시 발급된 리프레시 토큰을 데이터베이스에 저장하고, 만료 시간을 설정합니다.
 2. 리프레시 토큰 검증: 리프레시 토큰이 만료되거나 유효하지 않으면 토큰을 삭제하고, 사용자는 다시 로그인을 해야 합니다.
 3. 리프레시 토큰 만료 시 자동 로그아웃: 리프레시 토큰이 만료되면 서버는 로그아웃 처리 또는 강제 재로그인을 요구합니다.

- 클라이언트 측에서의 처리
리프레시 토큰이 만료되었을 경우 서버는 401 Unauthorized 상태 코드와 함께 "Refresh token expired" 메시지를 반환합니다.
클라이언트는 로그아웃 처리를 하고, 로그인 페이지로 리다이렉션하거나 다시 로그인을 요구하는 방식으로 처리할 수 있습니다.


---------------------------------------------------------------------------------------------------

[ ExpiredJwtException 메시지 ]

try {
    Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token);  // 만료된 토큰을 검증할 때
} catch (ExpiredJwtException e) {
    System.out.println(e.getMessage());
    // 출력 예시: JWT expired at 2024-09-18T14:04:02Z.
    Current time: 2024-09-25T14:04:02Z, a difference of 604800522 milliseconds.
    Allowed clock skew: 0 milliseconds.
}

ExpiredJwtException은 위와 같이 기본적으로 만료 시각, 현재 시각, 그리고 만료된 시간을 포함하는 상세한 메시지를 제공합니다.
이 메시지는 validateToken에서 잡히는 예외 메시지입니다.

따라서, "JWT expired"라는 문구는 ExpiredJwtException에 기본적으로 포함된 메시지이므로,
이 예외가 발생할 때 그 메시지를 통해 만료 사실을 알 수 있습니다.


----------------------------------------------------------------------------------------------------

구글 Oauth2 로그인
http://localhost:8080/oauth2/authorization/google


-----------------------------------------------------------------------------------------------------

- Product가 업데이트, 되지 않고 신규 생성되는 현상 발생

[ 문제의 코드 ]
public Product updateProduct(long id, UpdateProductRequest request) {
        return productRepository.findById(id)
                .map(existingProduct -> renewProduct(request))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
    }

    private Product renewProduct(UpdateProductRequest request) {
        Product updateProduct = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .brand(request.getBrand())
                .price(request.getPrice())
                .inventory(request.getInventory())
                .build();

        Category category = categoryRepository.findByName(request.getCategory().getName());
        updateProduct.setCategory(category);
        return productRepository.save(updateProduct);
    }

[ 문제 원인 ]
Product.builder()를 통해 새로운 Product 객체를 생성하고 있다.
그래서 기존의 제품을 수정하는 것이 아니라, 새로 생성된 Product가 저장되는 것이다.

[ 해결 방법 ]
기존 **existingProduct**를 업데이트해야 한다.
즉, Product.builder()로 새로운 객체를 만들지 말고, 기존 객체의 필드만 업데이트해야 한다.

public Product updateProduct(long id, UpdateProductRequest request) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    // 기존의 product를 업데이트
                    existingProduct.setName(request.getName());
                    existingProduct.setDescription(request.getDescription());
                    existingProduct.setBrand(request.getBrand());
                    existingProduct.setPrice(request.getPrice());
                    existingProduct.setInventory(request.getInventory());

                    // 카테고리도 업데이트
                    Category category = categoryRepository.findByName(request.getCategory().getName());
                    existingProduct.setCategory(category);

                    // 수정된 기존 product를 저장
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
    }
