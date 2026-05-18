package br.com.sisumoni.backend.service;


import br.com.sisumoni.backend.domain.Usuario;
import br.com.sisumoni.backend.dto.LoginRequest;
import br.com.sisumoni.backend.dto.LoginResponse;
import br.com.sisumoni.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private AuthenticationManager authenticationManager;
    private UsuarioRepository usuarioRepository;
    private JWTService jwtService;
    private PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request){
        // Autentica via Spring Security — lança exceção se credenciais inválidas
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        //Busca o usuário para montar a resposta
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + request.email()));

        String token = jwtService.gerarToken(usuario);

        return new LoginResponse(
            token,
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getPerfil().name()
        );
    }

    // Usado pelo seed para criar o admin inicial com senha encriptada
    public String encriptarSenha(String senha){
        return passwordEncoder.encode(senha);
    }
}
