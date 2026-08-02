package br.com.sisumoni.backend.controller;

import br.com.sisumoni.backend.dto.AlterarSenhaRequest;
import br.com.sisumoni.backend.dto.LoginRequest;
import br.com.sisumoni.backend.dto.LoginResponse;
import br.com.sisumoni.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // PUT /api/auth/senha — troca a própria senha (exige estar autenticado)
    @PutMapping("/senha")
    public ResponseEntity<Void> alterarSenha(@Valid @RequestBody AlterarSenhaRequest request) {
        authService.alterarSenha(request);
        return ResponseEntity.noContent().build();
    }
}