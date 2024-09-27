package com.example.coffeeOrderService.controller;

import com.example.coffeeOrderService.common.auth.jwt.JwtProvider;
import com.example.coffeeOrderService.common.auth.refreshToken.RefreshTokenRepository;
import com.example.coffeeOrderService.common.auth.refreshToken.RefreshTokenRequest;
import com.example.coffeeOrderService.common.exception.AlreadyExistsException;
import com.example.coffeeOrderService.common.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.common.auth.service.AuthService;

import com.example.coffeeOrderService.model.user.User;
import com.example.coffeeOrderService.request.AddUserRequest;
import com.example.coffeeOrderService.request.LogOutRequest;
import com.example.coffeeOrderService.request.LoginRequest;

import com.example.coffeeOrderService.response.ApiResponse;
import com.example.coffeeOrderService.response.JwtResponse;

import com.example.coffeeOrderService.service.user.UserService;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;

import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;


    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody AddUserRequest userRequest) {
        try {
            authService.signUp(userRequest);
            return ResponseEntity.ok().body(new ApiResponse("Registered Successfully!", null));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }


    // User(이메일 가입) 가 직접 로그아웃 할 때
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogOutRequest logoutRequest) {
        authService.logout(logoutRequest);
        return ResponseEntity.ok(new ApiResponse("Logged out successfully", null));
    }


    // OAuth 사용자 로그아웃
    @PostMapping("/oauth-logout")
    public ResponseEntity<?> oauthLogout(HttpServletRequest request) {
        authService.oauthLogout(request);
        return ResponseEntity.ok(new ApiResponse("Logged out successfully", null));
    }


//    @PostMapping("/login")
//    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
//        try {
//            Authentication authentication = authenticationManager
//                    .authenticate(new UsernamePasswordAuthenticationToken(
//                            request.getEmail(), request.getPassword()));
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            String jwt = jwtProvider.generateAccessTokenForUser(authentication);
//            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//            User user = (User) userDetails;
//            JwtResponse jwtResponse = new JwtResponse(user.getId(), jwt);
//            return ResponseEntity.ok(new ApiResponse("Login Successful", jwtResponse));
//        } catch (AuthenticationException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
//        }
//
//    }

    // 이메일 가입한 사용자 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            String jwt = authService.login(request);

            User user = authService.getAuthenticatedUser()
                    .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

            JwtResponse jwtResponse = new JwtResponse(user.getId(), jwt);
            return ResponseEntity.ok(new ApiResponse("Login Successful", jwtResponse));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
        }
    }


    // 리프레시 토큰으로 새로운 액세스 토큰 발급
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
        try {
            String newAccessToken = jwtProvider.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(new ApiResponse("Token refreshed successfully", newAccessToken));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Failed to refresh token", null));
        }
    }

}
