package com.example.coffeeOrderService.domain.user.service;

import com.example.coffeeOrderService.domain.auth.dto.AddUserRequest;
import com.example.coffeeOrderService.common.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.domain.user.entity.User;
import com.example.coffeeOrderService.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"+ userId));
    }

    public void save(AddUserRequest dto) {
        userRepository.save(User.builder()
                .email(dto.getEmail())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .build());
    }

    public User oauthSave(User entity) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        return userRepository.save(User.builder()
                .email(entity.getEmail())
                .password(encoder.encode(entity.getPassword()))
                .build());
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


}
