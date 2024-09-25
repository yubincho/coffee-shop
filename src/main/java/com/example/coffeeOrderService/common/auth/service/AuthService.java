package com.example.coffeeOrderService.common.auth.service;

import com.example.coffeeOrderService.common.auth.jwt.JwtProvider;
import com.example.coffeeOrderService.common.auth.refreshToken.RefreshToken;
import com.example.coffeeOrderService.common.auth.refreshToken.RefreshTokenRepository;
import com.example.coffeeOrderService.common.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.model.user.Role;
import com.example.coffeeOrderService.request.AddUserRequest;
import com.example.coffeeOrderService.common.exception.AlreadyExistsException;
import com.example.coffeeOrderService.model.user.User;
import com.example.coffeeOrderService.model.user.UserRepository;
import com.example.coffeeOrderService.request.LogOutRequest;
import com.example.coffeeOrderService.request.LoginRequest;
import com.example.coffeeOrderService.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.Set;


@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;

    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;


    @Transactional
    public void signUp(AddUserRequest addUserRequest) {
        userRepository.findByEmail(addUserRequest.getEmail()).ifPresent(it -> {
            throw new AlreadyExistsException("이미 존재하는 이메일입니다.");
        });

        if (!addUserRequest.getPassword().equals(addUserRequest.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호가 비밀번호 확인과 다릅니다.");
        }

        // 기본 Role 직접 생성 ("USER" 역할 부여)
        Role roleUser = new Role("USER");

        userRepository.save(User.builder()
                .email(addUserRequest.getEmail())
                .password(passwordEncoder.encode(addUserRequest.getPassword()))
                .roles(Set.of(roleUser))  // User 역할을 Set으로 설정
                .build());
    }


    @Transactional  // 트랜잭션을 적용하여 삭제 작업 처리
    public void logout(LogOutRequest logoutRequest) {
        User user = userService.findByEmail(logoutRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 해당 사용자의 리프레시 토큰 삭제 (트랜잭션 내에서 처리)
        refreshTokenRepository.deleteByUserId(user.getId());
    }


    @Transactional
    public String login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(
                () -> new ResourceNotFoundException("User not found!")
        );

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password");
        }

        // 인증 처리
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(), loginRequest.getPassword()));

        // SecurityContextHolder에 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtProvider.generateAccessTokenForUser(authentication);
        String refreshToken = jwtProvider.generateRefreshTokenForUser(user);

        // Refresh Token을 DB에 저장
        saveRefreshToken(user, refreshToken);

        return accessToken;
    }

    @Transactional
    protected void saveRefreshToken(User user, String refreshToken) {
        // 기존 리프레시 토큰 삭제
        refreshTokenRepository.deleteByUserId(user.getId());

        // 새로운 리프레시 토큰 저장
        RefreshToken token = new RefreshToken();
        token.setUserId(user.getId());
        token.setRefreshToken(refreshToken);
        token.setExpiryDate(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)).toInstant());
        refreshTokenRepository.save(token);
    }


    public Optional<User> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email);
    }

}
