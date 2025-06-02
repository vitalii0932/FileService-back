package org.example.fileservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Вимикаємо CSRF, оскільки використовуємо JWT
                .csrf(csrf -> csrf.disable())
                // Налаштовуємо CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Вимикаємо управління сесіями, оскільки JWT є stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Налаштовуємо правила доступу до ендпоінтів
                .authorizeHttpRequests(auth -> auth
                        // Захищаємо /api/public, вимагаємо аутентифікацію
                        .requestMatchers("/api/auth").authenticated()
                        // Інші ендпоінти (наприклад, /api/files/**) також можна захистити
                        .requestMatchers("/api/files/**").authenticated()
                        // Решта запитів дозволені (або налаштуйте за потреби)
                        .anyRequest().permitAll()
                )
                // Налаштовуємо OAuth2 Resource Server із JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    // Налаштування CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedHeaders(List.of("Content-Disposition", "Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Конвертер для JWT (можна розширити для витягнення додаткових claims, якщо потрібно)
    private org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter jwtAuthenticationConverter() {
        org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter converter = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter();
        return converter;
    }
}