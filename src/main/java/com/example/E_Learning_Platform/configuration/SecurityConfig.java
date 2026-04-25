/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.E_Learning_Platform.configuration;

import com.example.E_Learning_Platform.repository.InvalidatedTokenRepository;
import com.example.E_Learning_Platform.repository.RevokedSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 *
 * @author admin
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String secretKey;

    // application.yaml already sets context-path=/api/v1,
    // so /users here is exposed outside as /api/v1/users.
    private static final String[] SWAGGER_ENDPOINTS = {
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**"
    };

    private static final String[] PUBLIC_ENDPOINTS = {
        "/auth/**",
        "/payments/vnpay-return",
        "/error"
    };

    private static final String[] PUBLIC_POST_ENDPOINTS = {
        "/users",
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http.authorizeHttpRequests(request ->
                request.requestMatchers(SWAGGER_ENDPOINTS)
                        .permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS)
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS)
                        .permitAll()
                        .anyRequest()
                        .authenticated()
        );

        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
                );

        http.csrf(csrf -> csrf.disable());


        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
//    @Bean
//    public JwtDecoder jwtDecoder() {
//        SecretKey key = new SecretKeySpec(
//                secretKey.getBytes(),
//                "HmacSHA256"
//        );
//
//        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(key).build();
//
//        return token -> {
//            Jwt jwt = nimbusJwtDecoder.decode(token);
//
//            if (!"ACCESS".equals(jwt.getClaimAsString("tokenType"))) {
//                throw new JwtException("Invalid token type");
//            }
//
//            if (invalidatedTokenRepository.existsById(jwt.getId())) {
//                throw new JwtException("Token has been invalidated");
//            }
//
//            return jwt;
//        };
//    }

    @Bean
    public JwtDecoder jwtDecoder(RevokedSessionRepository revokedSessionRepository) {
        SecretKey key = new SecretKeySpec(
                secretKey.getBytes(),
                "HmacSHA256"
        );

        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(key).build();

        return token -> {
            Jwt jwt = nimbusJwtDecoder.decode(token);

            if (!"ACCESS".equals(jwt.getClaimAsString("tokenType"))) {
                throw new JwtException("Invalid token type");
            }

            if (invalidatedTokenRepository.existsById(jwt.getId())) {
                throw new JwtException("Token has been invalidated");
            }

            String sessionId = jwt.getClaimAsString("sessionId");
            if (sessionId == null || revokedSessionRepository.existsById(sessionId)) {
                throw new JwtException("Session has been revoked");
            }

            return jwt;
        };
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
