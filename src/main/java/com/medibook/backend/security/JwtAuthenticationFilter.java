package com.medibook.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Este filtro intercepta CADA request HTTP antes de llegar al Controller.
// Si la request trae un JWT válido en el header Authorization, autenticamos al usuario.
// OncePerRequestFilter garantiza que el filtro se ejecuta exactamente UNA vez por request.
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Buscamos el header "Authorization: Bearer <token>"
        final String authHeader = request.getHeader("Authorization");

        // 2. Si no hay header o no empieza con "Bearer ", dejamos pasar sin autenticar
        //    Spring Security rechazará el request si el endpoint requiere autenticación
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraemos el token (quitamos "Bearer " — 7 caracteres)
        final String token = authHeader.substring(7);
        String email;
        try {
            email = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Si el token tiene un email y el usuario AÚN no está autenticado en este request
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 5. Cargamos el usuario desde la BD (para verificar que sigue activo, roles, etc.)
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 6. Verificamos que el token sea válido (no expirado, corresponde al usuario)
            if (jwtUtil.isTokenValid(token, userDetails)) {
                // 7. Creamos el objeto de autenticación y lo ponemos en el SecurityContext
                //    Desde este momento, Spring sabe quién es el usuario en este request
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()   // los roles van aquí
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 8. Continuamos la cadena de filtros (el request llega al Controller)
        filterChain.doFilter(request, response);
    }
}