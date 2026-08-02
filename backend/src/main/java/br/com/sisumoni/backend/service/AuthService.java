package br.com.sisumoni.backend.service;

import br.com.sisumoni.backend.domain.Usuario;
import br.com.sisumoni.backend.dto.AlterarSenhaRequest;
import br.com.sisumoni.backend.dto.LoginRequest;
import br.com.sisumoni.backend.dto.LoginResponse;
import br.com.sisumoni.backend.exception.RegraDeNegocioException;
import br.com.sisumoni.backend.repository.UsuarioRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // @Lazy resolve a dependência circular com o SecurityConfig
    public AuthService(
            @Lazy AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.senha()
                )
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow();

        String token = jwtService.gerarToken(usuario);

        return new LoginResponse(
                token,
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().name(),
                usuario.isSenhaProvisoria()
        );
    }

    @Transactional
    public void alterarSenha(AlterarSenhaRequest request) {
        Usuario usuario = usuarioLogado();

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenhaHash())) {
            throw new RegraDeNegocioException("Senha atual incorreta");
        }

        usuario.setSenhaHash(passwordEncoder.encode(request.senhaNova()));
        usuario.setSenhaProvisoria(false);
        usuarioRepository.save(usuario);
    }

    private Usuario usuarioLogado() {
        return (Usuario) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}