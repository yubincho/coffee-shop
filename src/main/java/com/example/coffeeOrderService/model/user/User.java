package com.example.coffeeOrderService.model.user;


import com.example.coffeeOrderService.model.cart.Cart;
import com.example.coffeeOrderService.model.order.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "`user`")  // 테이블 이름을 백틱으로 감싸서 지정
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    private String email;
    private String password;

    private String nickname;

    private String address;

    /** ****************************************************************/

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;

    // order
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    // role
    @ManyToMany(fetch = FetchType.EAGER, cascade =
            {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))  //
    private Collection<Role> roles = new HashSet<>();


    // 배송지 변경 - Address entity ?
    public void changeAddress(String address) {
        this.address = address;
    }


}
