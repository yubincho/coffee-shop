package com.example.coffeeOrderService.model.user;


import com.example.coffeeOrderService.model.cart.Cart;
import com.example.coffeeOrderService.model.order.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NaturalId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "`user`")  // 테이블 이름을 백틱으로 감싸서 지정
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    private String email;
    private String password;

    private String nickname;

    private String address;

    private boolean isOAuth2;  // OAuth2 사용자인지 구분

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

    // Nickname 변경
    public User update(String nickname) {
        this.nickname = nickname;
        return this;
    }

    @Builder
    public User(String email, String password, Collection<Role> roles) {
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.isOAuth2 = false;  // 기본적으로 이메일/비밀번호 사용자는 OAuth2가 아님
    }

    // 새로운 생성자: 구글 OAuth2 로그인용 (비밀번호 대신 닉네임을 설정)
    public User(String email, String nickname, Collection<Role> roles, boolean isOAuth2) {
        this.email = email;
        this.nickname = nickname;
        this.roles = roles;
        this.isOAuth2 = isOAuth2;  // OAuth2 여부를 구분
        if (isOAuth2) {
            this.password = null;  // OAuth2 사용자는 비밀번호가 없음
        }
    }


    /** Security ******************************************************************************* */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
