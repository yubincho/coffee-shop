package com.example.coffeeOrderService.service.user;

import com.example.coffeeOrderService.exception.ResourceNotFoundException;
import com.example.coffeeOrderService.model.user.User;
import com.example.coffeeOrderService.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;


    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }
}
