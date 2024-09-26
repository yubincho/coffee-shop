package com.example.coffeeOrderService.controller;

import com.example.coffeeOrderService.common.auth.jwt.JwtFactory;
import com.example.coffeeOrderService.common.auth.refreshToken.RefreshToken;
import com.example.coffeeOrderService.common.auth.refreshToken.RefreshTokenRepository;
import com.example.coffeeOrderService.common.auth.service.AuthService;
import com.example.coffeeOrderService.model.user.Role;
import com.example.coffeeOrderService.model.user.User;
import com.example.coffeeOrderService.model.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String secretKey = "e/KB0JGb/5xfLxDh7qPc5yKzNEJhHNYrMHphqCv+WOo=";

    User user;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setSecurityContext() {
        userRepository.deleteAll();
        user = userRepository.save(User.builder()
                .email("user@gmail.com")
                .password("test")
                        .roles(Set.of(new Role("USER")))
                .build());

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    }

    @DisplayName("구글 사용자 로그아웃시 RefreshToken 삭제된다.")
    @Test
    void oauth_user_logout() throws Exception {
        // given
        final String url = "/api/v1/users/oauth-logout";

        String refreshToken = createRefreshToken();

        System.out.println("[userId]" + user.getId());
        refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken));

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, refreshToken, user.getAuthorities()));
        System.out.println("[user.getId()]" + user.getId());

        // when
        // Authorization 헤더에 JWT 토큰을 포함하여 요청
        ResultActions resultActions = mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + refreshToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        // then
        resultActions.andExpect(status().isOk());

        assertThat(refreshTokenRepository.findByRefreshToken(refreshToken)).isEmpty();
    }


    private String createRefreshToken() {
        // user 객체가 null인지 확인
        Objects.requireNonNull(user, "User 객체가 null입니다.");

        // user.getId()가 null인지 확인
        Long userId = user.getId();
        Objects.requireNonNull(userId, "User ID가 null입니다.");

        // JWT 토큰 만료 시간 (예: 14일)
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + Duration.ofDays(14).toMillis());

        // JWT 토큰 생성
        return Jwts.builder()
                .setSubject(user.getEmail())  // user의 이메일을 subject로 설정
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .addClaims(Map.of("id", userId))  // user ID를 claims에 포함
                .signWith(SignatureAlgorithm.HS256, secretKey)  // 서명
                .compact();  // 토큰 압축
    }
}
