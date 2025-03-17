package pl.lodz.p.it.expenseTracker.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.cors.CorsConfiguration;
import pl.lodz.p.it.expenseTracker.security.JwtAuthenticationFilter;
import pl.lodz.p.it.expenseTracker.security.JwtAuthenticationService;

import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationService authService;

    private final UserDetailsService service;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(antMatcher("/api/**"))
                .authorizeHttpRequests( request ->
                        request.requestMatchers(
                                "/api/accounts",
                                "/api/auth/account/admin/register",
                                "/api/accounts/account/{id}/enable",
                                "/api/accounts/account/{id}/disable",
                                "/api/accounts/account/{id}/inactive"
                        ).hasAuthority("ADMIN"))
                .authorizeHttpRequests( request ->
                        request.requestMatchers(
                                "/api/transactions/*",
                                "/api/groups/*",
                                "/api/categories/*",
                                "/api/analysis/*"
                        ).hasAuthority("USER"))
                .authorizeHttpRequests( request ->
                        request.requestMatchers(
                                "/api/accounts/account",
                                "/api/accounts/account/{id}",
                                "/api/accounts/account/{email}",
                                "/api/accounts/users",
                                "/api/auth/account/refresh-token",
                                "/api/accounts/account/{id}/language",
                                "/api/accounts/account/{id}/email",
                                "/api/accounts/account/{id}/active"
                        ).hasAnyAuthority("USER", "ADMIN"))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(
                                        "/v3/api-docs",
                                        "/swagger-resources",
                                        "/swagger-resources/**",
                                        "/configuration/ui",
                                        "/configuration/security",
                                        "/swagger-ui.html",
                                        "/webjars/**",
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/error",

                                        //authentication-controller
                                        "/api/auth/account/register",
                                        "/api/auth/account/register/confirm",
                                        "/api/auth/account/authenticate",
                                        "/api/auth/account/authenticate/forgot-password",
                                        "/api/auth/account/authenticate/forgot-password/confirm",
                                        "/api/auth/account/admin/register/confirm",
                                        "/api/auth/account/authenticate/reset-password/confirm",
                                        "/api/auth/account/authenticate/reset-password",
                                        "/api/accounts/account/{id}/email/confirm"

                                ).permitAll()
                                .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(authService, service),
                        UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(List.of("*"));
                    configuration.setAllowedMethods(List.of("*"));
                    configuration.setAllowedHeaders(List.of("*"));
                    return configuration;
                }))
                .build();
    }
}