package com.coffee_shop.coffee_shop.security.auth;

import com.coffee_shop.coffee_shop.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AuthUser implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String roleName;
    private final Boolean active;
    private final List<GrantedAuthority> authorities;

    public AuthUser(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.roleName = user.getRole().getName();
        this.active = user.getIsActive();

//        this.authorities = user.getRole().getPermissions().stream()
//                .map(p -> (GrantedAuthority) new SimpleGrantedAuthority(p.getName()))
//                .collect(Collectors.toList());
//        this.authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
        List<GrantedAuthority> permissionAuthorities = user.getRole().getPermissions().stream()
                .map(p -> (GrantedAuthority) new SimpleGrantedAuthority(p.getName()))
                .toList();

        this.authorities = new ArrayList<>(permissionAuthorities); // guaranteed mutable
        this.authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
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
        return active;
    }
}