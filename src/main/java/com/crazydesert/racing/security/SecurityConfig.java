package com.crazydesert.racing.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Authentication is required"
                                )
                        )
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(
                                        HttpServletResponse.SC_FORBIDDEN,
                                        "Access denied"
                                )
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/races", "/races/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/avatars/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/media/images/*").permitAll()

                        .requestMatchers("/desert-live/admin/**").hasRole("ADMIN")
                        .requestMatchers("/desert-live/my/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/desert-live").permitAll()
                        .requestMatchers(HttpMethod.GET, "/desert-live/random").permitAll()
                        .requestMatchers(HttpMethod.GET, "/desert-live/images/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/desert-live/*").permitAll()

                        .requestMatchers("/users/me").authenticated()
                        .requestMatchers("/users/me/**").authenticated()

                        .requestMatchers("/users/*/make-admin").hasRole("ADMIN")
                        .requestMatchers("/users/*/verify-license").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/*/cars").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/races").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/races/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/races/*").hasRole("ADMIN")

                        .requestMatchers("/race-cars/my").authenticated()
                        .requestMatchers(HttpMethod.POST, "/race-cars/*/owner/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/race-cars").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/race-cars").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/race-cars/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/race-cars/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/race-cars/*").authenticated()

                        .requestMatchers(HttpMethod.POST, "/registrations/my").authenticated()
                        .requestMatchers(HttpMethod.POST, "/registrations").hasRole("ADMIN")
                        .requestMatchers("/registrations/**").authenticated()

                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.disable());

        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
