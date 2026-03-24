package com.medibook.backend.service.impl;

import com.medibook.backend.dto.auth.AuthResponse;
import com.medibook.backend.dto.auth.LoginRequest;
import com.medibook.backend.dto.auth.RegisterRequest;
import com.medibook.backend.exception.ConflictException;
import com.medibook.backend.model.Rol;
import com.medibook.backend.model.Usuario;
import com.medibook.backend.repository.RolRepository;
import com.medibook.backend.repository.UsuarioRepository;
import com.medibook.backend.security.JwtUtil;
import com.medibook.backend.service.AuthService;
import com.medibook.backend.service.MedicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;   // valida email+password
    private final JwtUtil jwtUtil;                     // genera tokens
    private final UserDetailsService userDetailsService;

    @Override
    public AuthResponse login(LoginRequest request) {
        // Spring Authentication Manager verifica credenciales contra la BD automáticamente.
        // Si son incorrectas, lanza BadCredentialsException (capturada por GlobalExceptionHandler).
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Si llegamos aquí, las credenciales son correctas
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();

        return buildResponse(token, usuario);
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Validamos que el email no exista ANTES de intentar guardar
        // (evitamos excepciones de constraint de BD que son más difíciles de manejar)
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email ya está registrado: " + request.getEmail());
        }

        // El registro público SIEMespecialidadRepository.findByNombre(requestPRE asigna ROLE_PACIENTE — decisión de seguridad
        Rol rol = rolRepository.findByNombre("ROLE_PACIENTE")
                .orElseThrow(() -> new RuntimeException("Rol ROLE_PACIENTE no encontrado. ¿Ejecutó el DataInitializer?"));

        Set<Rol> roles = new HashSet<>();
        roles.add(rol);

        // passwordEncoder.encode() aplica BCrypt — NUNCA guardes texto plano
        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre())
                .apellidos(request.getApellidos())
                .activo(true)
                .roles(roles)
                .build();

        usuario = usuarioRepository.save(usuario);

        // Generamos token inmediatamente: el usuario queda logueado tras registrarse
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return buildResponse(token, usuario);
    }

    // Helper para no repetir el builder en login y register
    private AuthResponse buildResponse(String token, Usuario usuario) {
        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellidos(usuario.getApellidos())
                .roles(usuario.getRoles().stream().map(Rol::getNombre).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public AuthResponse crearAdmin(RegisterRequest request) {
        // Verificamos que el email no exista ya en el sistema
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }

        // A diferencia de register(), aquí asignamos ROLE_ADMIN
        Rol rol = rolRepository.findByNombre("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Rol ROLE_ADMIN no encontrado"));

        Set<Rol> roles = new HashSet<>();
        roles.add(rol);

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre())
                .apellidos(request.getApellidos())
                .activo(true)
                .roles(roles)
                .build();

        usuario = usuarioRepository.save(usuario);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return buildResponse(token, usuario);
    }
}