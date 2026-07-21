package com.example.coffeeOrderService.domain.auth.jwt;

import com.example.coffeeOrderService.domain.auth.refreshToken.RefreshTokenRepository;
import com.example.coffeeOrderService.common.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.domain.user.entity.User;
import com.example.coffeeOrderService.domain.user.repository.UserRepository;
import com.example.coffeeOrderService.domain.auth.refreshToken.RefreshToken;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;q

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.List;


@RequiredArgsConstructor
@Component
public class JwtProvider {

    @Value("${auth.token.jwtSecret}")
    private String jwtSecret;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // 토큰 유효기간
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);  // Access Token: 1일
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(7);  // Refresh Token: 7일

    // Access Token 생성
    public String generateAccessTokenForUser(User user) {
        return generateToken(user, ACCESS_TOKEN_DURATION);
    }

    // Refresh Token 생성
    public String generateRefreshTokenForUser(User user) {
        return generateToken(user, REFRESH_TOKEN_DURATION);
    }

    // 공통 토큰 생성 메서드
    private String generateToken(User user, Duration expirationTime) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime.toMillis()))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new JwtException(e.getMessage());
        }
    }

    // 리프레시 토큰으로 Access Token 갱신
    // 재발급 — userService.getUserById → userRepository 조회로 변경
    public String refreshAccessToken(String refreshToken) {
        // Refresh Token 검증
        RefreshToken token = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new JwtException("Invalid refresh token"));

        // 만료 여부 확인
        if (token.getExpiryDate().isBefore(new Date().toInstant())) {
            refreshTokenRepository.delete(token);  // 만료된 토큰 삭제
            throw new JwtException("Refresh token expired. Please login again.");
        }

        // Refresh Token의 사용자 정보로 새로운 Access Token 생성
        Long userId = token.getUserId();
        User user = userRepository.findById(userId)  // UserService 대신 Repository
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return generateAccessTokenForUser(user);
    }

    // JWT에서 유저네임 추출 -> email 추출로 설정함
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Secret 키 생성
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}

