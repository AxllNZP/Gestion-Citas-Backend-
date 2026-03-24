package com.medibook.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// Esta clase es el "gestor de tokens JWT"
// Un JWT tiene 3 partes separadas por puntos: HEADER.PAYLOAD.SIGNATURE
// - Header: algoritmo de firma
// - Payload: datos (email, expiración, etc.)
// - Signature: garantiza que el token no fue alterado
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // Convertimos el secret string en una Key criptográfica real
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Genera un token JWT para un usuario autenticado
    // El "subject" del token es el email del usuario
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())           // username = email en nuestro caso
                .setIssuedAt(new Date())                        // cuándo fue creado
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // cuándo expira
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extrae el email del token (sin necesidad de consultarlo a la BD)
    // Esta es la "magia" del JWT: la info viaja dentro del token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Valida que el token pertenezca al usuario y no esté expirado
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Método genérico para extraer cualquier campo del payload del token
    // Function<Claims, T> es una función que recibe los claims y devuelve T
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)  // también verifica la firma aquí
                .getBody();
        return resolver.apply(claims);
    }
}