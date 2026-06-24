package com.banking.Banking.Entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

/**
 * Класс для хранения данных текущего авторизированного пользователя
 */
@Getter
public class SessionUser implements UserDetails {
    private final Long id;
    private final String login;
    private final String password;
    private final String phone;
    private final Collection<? extends GrantedAuthority> authorities;
    public SessionUser(Client client) {
        this.id = client.getId();
        this.login = client.getLogin();
        this.password = client.getPassword();
        this.authorities = client.getAuthorities();
        this.phone = client.getPhone();
    }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    @Override public String getUsername() {
        return login;
    }
    @Override public String getPassword() {
        return password;
    }
    @Override public boolean isAccountNonExpired() {
        return true;
    }
    @Override public boolean isAccountNonLocked() {
        return true;
    }
    @Override public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override public boolean isEnabled() {
        return true;
    }
}
