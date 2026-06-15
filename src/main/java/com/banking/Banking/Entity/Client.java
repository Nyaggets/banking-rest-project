package com.banking.Banking.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "client")
@Builder
public class Client implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    Long id;
    @Size(min = 11, max = 11)
    String phone;
    @Size(min = 5, max = 40)
    String surname;
    @Size(min = 2, max = 20)
    String name;
    @Size(min = 7, max = 30)
    String patronymic;
    String password;
    @Size(min = 4, max = 20)
    String login;
    String authority;
    @Column(name = "passport_series")
    String passportSeries;
    @Column(name = "passport_number")
    String passportNumber;
    @Column(name = "passport_issue_date")
    String passportIssueDate;
    @Column(name = "passport_issued_by")
    String passportIssuedBy;
    @Column(name = "passport_department_code")
    String passportDepartmentCode;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.authority));
    }

    @Override
    public String getUsername() {
        return this.login;
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

    public String getFullName() {
        StringBuilder fullName = new StringBuilder(String.format("%s %s", this.surname, this.name));
        return this.patronymic != null
                ? fullName.append(String.format(" %s", this.patronymic)).toString()
                : fullName.toString();
    }

    public String getShortenFullName() {
        StringBuilder fullName = new StringBuilder(String.format("%s %s.", this.surname, this.name.charAt(0)));
        return this.patronymic != null
                ? fullName.append(String.format(" %s.", this.patronymic.charAt(0))).toString()
                : fullName.toString();
    }
}
