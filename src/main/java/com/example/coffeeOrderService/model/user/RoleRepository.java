package com.example.coffeeOrderService.model.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Object> findByName(String roleUser);
}
