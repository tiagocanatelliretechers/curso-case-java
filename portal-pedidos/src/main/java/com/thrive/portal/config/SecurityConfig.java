package com.thrive.portal.config;

import com.thrive.portal.security.JwtAuthFilter;
import com.thrive.portal.security.MigratingPasswordEncoder;
import com.thrive.portal.security.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracao de seguranca ENDURECIDA (branch solucao-hardened).
 * Aplica: Lab 3.3 (BCrypt), Lab 3.4 (rate limit), Lab 3.5 (/admin ADMIN),
 * Lab 4.2 (JWT verificado), Lab 4.3 (sessao), Lab 4.4 (CSRF), Lab 5.1 (headers),
 * Lab 6.1 (Actuator).
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, RateLimitFilter rateLimitFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new MigratingPasswordEncoder();   // Lab 3.3 (BCrypt + migracao MD5)
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Lab 4.4 - CSRF habilitado para a web; API stateless (token em header) isenta
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", "/login", "/registrar", "/esqueci-senha",
                        "/css/**", "/js/**", "/webjars/**",
                        "/api/auth/**", "/produtos", "/produtos/buscar")
                    .permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")   // Lab 6.1
                .requestMatchers("/admin/**").hasRole("ADMIN")       // Lab 3.5
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/pedidos", true)
                .permitAll()
            )
            .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
            // Lab 4.3 - regeneracao de ID de sessao no login (anti fixation)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(fixation -> fixation.changeSessionId())
            )
            // Lab 5.1 - headers de seguranca
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true))
            )
            // Lab 3.4 - rate limit antes da autenticacao; Lab 4.2 - JWT
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
