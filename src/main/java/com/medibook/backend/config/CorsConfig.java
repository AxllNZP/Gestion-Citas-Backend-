package com.medibook.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

// CORS (Cross-Origin Resource Sharing) es un mecanismo de seguridad del navegador.
// El navegador BLOQUEA requests de http://localhost:4200 (Angular) a http://localhost:8080 (Spring)
// por ser "orígenes distintos". Este config le dice a Spring que PERMITA ese origen.
// Sin esto, el frontend recibiría un error de CORS aunque el backend funcione bien.
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Solo permitimos el origen de nuestro frontend Angular
        // En producción esto sería tu dominio real: "https://tu-app.com"
        config.setAllowedOrigins(List.of("http://localhost:4200"));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Permitimos todos los headers (incluyendo "Authorization" para el JWT)
        config.setAllowedHeaders(List.of("*"));

        // Necesario para que el frontend pueda enviar cookies o el header Authorization
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // aplica a TODOS los endpoints
        return new CorsFilter(source);
    }
}