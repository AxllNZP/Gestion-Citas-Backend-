package com.medibook.backend.config;

import com.medibook.backend.security.JwtAuthenticationFilter;
import com.medibook.backend.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @EnableMethodSecurity habilita @PreAuthorize en los Controllers
// Sin esto, las anotaciones @PreAuthorize("hasRole(...)") no funcionarían
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitamos CSRF porque usamos JWT (stateless), no cookies de sesión
                .csrf(csrf -> csrf.disable())

                // Política de sesión STATELESS: cada request es independiente con su JWT
                // El servidor NO guarda sesiones. Esto es escalable y necesario para REST APIs.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Endpoints PÚBLICOS - no requieren token
                        .requestMatchers("/api/auth/**").permitAll()
                        // GET de especialidades y médicos es público (para que el paciente pueda ver antes de registrarse)
                        .requestMatchers(HttpMethod.GET, "/api/especialidades/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/medicos/**").permitAll()
                        // Todo lo demás requiere estar autenticado
                        // Los roles específicos se manejan con @PreAuthorize en cada Controller
                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider())

                // Insertamos nuestro filtro JWT ANTES del filtro estándar de Spring Security
                // Orden importa: primero validamos el JWT, luego Spring hace su proceso normal
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // AuthenticationProvider une UserDetailsService + PasswordEncoder
    // Spring lo usa al llamar a authManager.authenticate(...)
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // AuthenticationManager lo usamos en AuthServiceImpl para validar login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // BCrypt es el estándar para hashear contraseñas. NUNCA guardes contraseñas en texto plano.
    // BCrypt agrega un "salt" automáticamente → dos usuarios con la misma contraseña tienen hashes distintos
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}