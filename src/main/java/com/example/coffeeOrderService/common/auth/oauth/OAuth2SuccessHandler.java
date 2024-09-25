package com.example.coffeeOrderService.common.auth.oauth;

import com.example.coffeeOrderService.common.auth.jwt.JwtProvider;
import com.example.coffeeOrderService.common.auth.refreshToken.RefreshToken;
import com.example.coffeeOrderService.common.auth.refreshToken.RefreshTokenRepository;
import com.example.coffeeOrderService.common.auth.util.CookieUtil;
import com.example.coffeeOrderService.common.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.model.user.Role;
import com.example.coffeeOrderService.model.user.RoleRepository;
import com.example.coffeeOrderService.model.user.User;
import com.example.coffeeOrderService.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;


@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(7);

    public static final String REDIRECT_PATH = "/api/v1/products/all";

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        // 이메일로 User 조회
        User user = userService.findByEmail((String) oAuth2User.getAttributes().get("email"))
                .orElseGet(() -> {
                    User newUser = new User();
                    String email = (String) oAuth2User.getAttributes().getOrDefault("email", null);
                    String name = (String) oAuth2User.getAttributes().getOrDefault("name", null);

                    if (email == null) {
                        throw new IllegalArgumentException("OAuth2 사용자 정보에서 이메일을 찾을 수 없습니다.");
                    }

                    Role defaultRole = (Role) roleRepository.findByName("ROLE_USER")
                            .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
                    newUser.getRoles().add(defaultRole);

                    return userService.oauthSave(newUser);
                });

        String refreshToken = jwtProvider.generateRefreshTokenForUser(user);
        saveRefreshToken(user.getId(), refreshToken);
        addRefreshTokenToCookie(request, response, refreshToken);

        String accessToken = jwtProvider.generateAccessTokenForUser(authentication);
        String targetUrl = getTargetUrl(accessToken);

        clearAuthenticationAttributes(request, response);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .map(entity -> entity.update(newRefreshToken))
                .orElseGet(() -> {
                    RefreshToken token  = new RefreshToken();
                    token.setUserId(userId);
                    token.setRefreshToken(newRefreshToken);
                    token.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
                    return token;
                });

        refreshTokenRepository.save(refreshToken);
    }

    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();

        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private String getTargetUrl(String token) {
        return UriComponentsBuilder.fromUriString(REDIRECT_PATH)
                .queryParam("token", token)
                .build()
                .toUriString();
    }


}
