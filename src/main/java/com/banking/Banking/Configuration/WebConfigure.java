package com.banking.Banking.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class WebConfigure implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/main").setViewName("main");
        registry.addViewController("/transfer").setViewName("transfer");
        registry.addViewController("/history").setViewName("history");
        registry.addViewController("/transaction").setViewName("transaction");
        registry.addViewController("/profile").setViewName("profile");
        registry.addViewController("/card").setViewName("card");
        registry.addViewController("/balance-top-up").setViewName("balance-top-up");
        registry.addViewController("/session-expired").setViewName("/session-expired");
    }
}
