package br.com.sisumoni.backend.service;

import br.com.sisumoni.backend.domain.Usuario;
import br.com.sisumoni.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.senha}")
    private String adminSenha;

    public AdminSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Só cria se não existir nenhum admin — evita duplicata
        if (!usuarioRepository.existsByPerfil(Usuario.Perfil.ADMIN)) {
            Usuario admin = Usuario.builder()
                    .nome("Administrador")
                    .email(adminEmail)
                    .senhaHash(passwordEncoder.encode(adminSenha))
                    .perfil(Usuario.Perfil.ADMIN)
                    .ativo(true)
                    .build();

            usuarioRepository.save(admin);
            System.out.println("✅ Admin inicial criado: " + adminEmail);
        } else {
            System.out.println("✅ Admin já existe — seed ignorado");
        }
    }
}