package com.example.coffeeOrderService.common.auth.oauth;

import com.example.coffeeOrderService.model.user.Role;
import com.example.coffeeOrderService.model.user.RoleRepository;
import com.example.coffeeOrderService.model.user.User;
import com.example.coffeeOrderService.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);
        saveOrUpdate(user);
        return user;
    }

    private User saveOrUpdate(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");  // 닉네임으로 사용할 값

        Role defaultRole = (Role) roleRepository.findByName("USER").orElseGet(() -> new Role("USER"));

        User user = userRepository.findByEmail(email)
                .map(entity -> entity.update(name))  // 기존 사용자라면 닉네임 업데이트
                .orElseGet(() -> {
                    Set<Role> roles = new HashSet<>();
                    roles.add(defaultRole);
                    return new User(email, name, roles, true);   // OAuth2 사용자는 true로 설정
                });
        return userRepository.save(user);
    }
}
