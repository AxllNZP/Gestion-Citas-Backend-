package com.medibook.backend.security;

import com.medibook.backend.model.Usuario;
import com.medibook.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

// Spring Security usa esta clase para cargar el usuario desde la BD.
// Es el "puente" entre tu entidad Usuario y el sistema de seguridad de Spring.
// Spring llama a loadUserByUsername automáticamente durante la autenticación.
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Buscamos por email (en Spring Security el "username" es el identificador único)
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Convertimos nuestra entidad Usuario al UserDetails que Spring Security entiende
        // Los roles se convierten en "GrantedAuthority" — Spring los usa para @PreAuthorize
        return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.isActivo(),    // enabled: si está false, Spring rechaza el login
                true,                  // accountNonExpired
                true,                  // credentialsNonExpired
                true,                  // accountNonLocked
                usuario.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getNombre()))
                        .collect(Collectors.toList())
        );
    }
}