package com.thrive.portal.config;

import com.thrive.portal.security.JwtAuthFilter;
import com.thrive.portal.security.Md5PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracao de seguranca BASELINE (propositalmente fraca).
 *
 * Vulnerabilidades plantadas aqui:
 *  - A01: /admin/** exige apenas autenticacao (qualquer usuario logado),
 *         nao o papel ROLE_ADMIN  -> Lab 3.5
 *  - A05: /h2-console e /actuator liberados sem autenticacao
 *  - Session fixation desligada (.none())               -> Lab 4.3
 *  - CSRF desabilitado globalmente sem justificativa     -> Lab 4.4
 *  - PasswordEncoder MD5                                  -> Lab 3.3
 *  - Sem rate limiting no login                           -> Lab 3.4
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Md5PasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // A08/sessao: CSRF desabilitado globalmente
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", "/login", "/registrar", "/esqueci-senha",
                        "/css/**", "/js/**", "/webjars/**",
                        "/h2-console/**", "/actuator/**",
                        "/api/auth/**", "/produtos/buscar", "/produtos")
                    .permitAll()
                // A01: deveria ser hasRole("ADMIN")
                .requestMatchers("/admin/**").authenticated()
                .requestMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/pedidos", true)
                .permitAll()
            )
            .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
            // sessao "stateful", mas sem protecao contra fixation
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(fixation -> fixation.none())
            )
            // necessario para o H2 console renderizar em frame
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
