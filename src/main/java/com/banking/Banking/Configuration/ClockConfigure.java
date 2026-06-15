package com.banking.Banking.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Конфигурация часов
 * Унифицированный источник текущего времени для всех сервисов
 */
@Configuration
public class ClockConfigure {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}