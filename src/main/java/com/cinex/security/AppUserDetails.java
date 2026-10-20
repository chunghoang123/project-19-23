package com.cinex.security;

import com.cinex.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Bọc {@link com.cinex.domain.User} cho Spring Security.
 */
@Getter
public class AppUserDetails implements UserDetails {

    private final Integer id;
    private final String name;
    private final String email;
    private final String password;
    private final String phone;
    private final boolean enabled;
    private final List<GrantedAuthority> authorities;

    public AppUserDetails(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.phone = user.getPhone();
        this.enabled = !"blocked".equalsIgnoreCase(user.getStatus());
        this.authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().toUpperCase()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email != null ? email : phone;
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
        return enabled;
    }
}
