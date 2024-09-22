package com.example.coffeeOrderService.model.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.HashSet;


@NoArgsConstructor
@Setter
@Getter
@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;  // role name


    public Role(String name) {
        this.name = name;
    }


    @ManyToMany(mappedBy = "roles")
    private Collection<User> users = new HashSet<>();
}
