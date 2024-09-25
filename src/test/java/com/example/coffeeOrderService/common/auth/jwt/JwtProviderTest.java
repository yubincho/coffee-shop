package com.example.coffeeOrderService.common.auth.jwt;

import com.example.coffeeOrderService.model.user.User;
import com.example.coffeeOrderService.model.user.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    private final String secretKey = "e/KB0JGb/5xfLxDh7qPc5yKzNEJhHNYrMHphqCv+WOo=";

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @DisplayName("generateToken(): 토큰 생성")
    @Test
    @Transactional
    void generateToken() {
        // 권한 설정
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // 실제 DB에 User 객체 저장
        User testUser = userRepository.save(User.builder()
                .email("user2@gmail.com")
                .password("1111")
                .build());

        // Authentication 객체 생성 (UserDetails 사용)
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testUser, null, authorities);

        // 토큰 생성
        String token = jwtProvider.generateAccessTokenForUser(authentication);

        // 토큰에서 사용자 ID 추출
        Long userId = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id", Long.class);

        // 사용자 ID 확인
        assertThat(userId).isEqualTo(testUser.getId());
    }


    @DisplayName("validToken(): 기한 민료 토큰은 유효성 검사에 실패한다.")
    @Test
    void invalidToken() {
        // 7일 전에 만료된 토큰
        String token = JwtFactory.builder()
                .expiration(new Date(new Date().getTime() - Duration.ofDays(7).toMillis())) // 현재 시간으로부터 7일 전
                .build()
                .createToken(secretKey);

        // validateToken이 JwtException을 던지는지 확인
        JwtException exception = assertThrows(JwtException.class, () -> {
            jwtProvider.validateToken(token);
        });

        // 예외 메시지 확인 (선택사항)
        assertThat(exception.getMessage()).contains("JWT expired");
    }


    @DisplayName("validToken(): 기한 민료 전 토큰은 유효성 검증에 성공한다.")
    @Test
    void validToken() {
        String token = JwtFactory.withDefaultValues().createToken(secretKey);

        boolean result = jwtProvider.validateToken(token);

        assertThat(result).isTrue();
    }


    @DisplayName("getUsernameFromToken(): 토큰으로 인증 정보를 가져온다.")
    @Test
    void getUsernameFromToken() {
        String userEmail = "user2@gmail.com";
        String token = JwtFactory.builder()
                .subject(userEmail)
                .build()
                .createToken(secretKey);

        String email = jwtProvider.getUsernameFromToken(token);
        assertThat(email).isEqualTo(userEmail);
    }

}