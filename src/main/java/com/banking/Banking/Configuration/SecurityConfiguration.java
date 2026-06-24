package com.banking.Banking.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация авторизации и выхода
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/login*", "/logout", "/static/**", "/js/**", "/js/utils/**", "/session-expired", "/*.png", "/*.ico", "/*.jpg", "/*.css", "/*.js"  ).permitAll()
                    .requestMatchers("/main", "/transfer", "/history", "/profile", "/card", "/transaction", "/balance-deposit").hasAuthority("USER")
                    .anyRequest().authenticated()
                )
                .formLogin(login -> login
                    .loginPage("/login")
                    .defaultSuccessUrl("/main", true)
                    .failureHandler((request, response, exception) -> {
                        response.setStatus(400);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"errors\":{\"login\":\"Неверный логин или пароль\"}}");
                    })
                    .permitAll()
                )
                 .logout(logout -> logout
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
                )
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        String uri = request.getRequestURI();
                        if (uri.startsWith("/api")) {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"errors\":{\"message\":\"Сессия истекла.\"}}");
                        } else {
                            response.sendRedirect("/session-expired");
                        }
                    })
                );
        return http.build();
    }
}
