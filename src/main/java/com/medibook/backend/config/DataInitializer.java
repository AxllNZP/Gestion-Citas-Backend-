package com.medibook.backend.config;

import com.medibook.backend.model.Rol;
import com.medibook.backend.model.Usuario;
import com.medibook.backend.repository.RolRepository;
import com.medibook.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

// CommandLineRunner: Spring ejecuta el método run() automáticamente al arrancar la aplicación.
// Esto es ideal para poblar la BD con datos iniciales necesarios para que el sistema funcione.
// Usamos verificaciones "si no existe" para que sea IDEMPOTENTE — se puede reiniciar sin duplicar datos.
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("=== Iniciando DataInitializer ===");

        // 1. Crear los 3 roles base del sistema
        crearRolSiNoExiste("ROLE_ADMIN");
        crearRolSiNoExiste("ROLE_MEDICO");
        crearRolSiNoExiste("ROLE_PACIENTE");

        // 2. Crear el usuario administrador por defecto
        // En producción cambiarías estas credenciales mediante variables de entorno
        if (!usuarioRepository.existsByEmail("admin@medibook.com")) {
            Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN").orElseThrow();

            Set<Rol> roles = new HashSet<>();
            roles.add(rolAdmin);

            Usuario admin = Usuario.builder()
                    .email("admin@medibook.com")
                    .password(passwordEncoder.encode("Admin1234!"))  // BCrypt encripta aquí
                    .nombre("Admin")
                    .apellidos("MediBook")
                    .activo(true)
                    .roles(roles)
                    .build();

            usuarioRepository.save(admin);
            log.info("✅ Admin creado -> Email: admin@medibook.com | Password: Admin1234!");
        } else {
            log.info("✅ Admin ya existe, saltando...");
        }

        log.info("=== DataInitializer completado ===");
    }

    private void crearRolSiNoExiste(String nombre) {
        if (rolRepository.findByNombre(nombre).isEmpty()) {
            rolRepository.save(Rol.builder().nombre(nombre).build());
            log.info("✅ Rol creado: {}", nombre);
        } else {
            log.info("✅ Rol '{}' ya existe, saltando...", nombre);
        }
    }
}