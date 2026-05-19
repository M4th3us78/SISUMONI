package br.com.sisumoni.backend.exception;

import br.com.sisumoni.backend.dto.ErroResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacao(
            MethodArgumentNotValidException ex) {

        List<String> detalhes = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErroResponse(400, "Erro de validação",
                        "Um ou mais campos estão inválidos",
                        LocalDateTime.now(), detalhes));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroResponse> handleCredenciaisInvalidas(
            BadCredentialsException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErroResponse(401, "Não autorizado",
                        "Email ou senha incorretos",
                        LocalDateTime.now(), null));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErroResponse> handleContaDesativada(
            DisabledException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErroResponse(401, "Conta inativa",
                        "Sua conta foi desativada. Entre em contato com o administrador",
                        LocalDateTime.now(), null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> handleAcessoNegado(
            AccessDeniedException ex) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErroResponse(403, "Acesso negado",
                        "Você não tem permissão para acessar este recurso",
                        LocalDateTime.now(), null));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrado(
            RecursoNaoEncontradoException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErroResponse(404, "Não encontrado",
                        ex.getMessage(),
                        LocalDateTime.now(), null));
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> handleRegraDeNegocio(
            RegraDeNegocioException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErroResponse(409, "Conflito",
                        ex.getMessage(),
                        LocalDateTime.now(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleErroGeral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErroResponse(500, "Erro interno",
                        "Ocorreu um erro inesperado. Tente novamente",
                        LocalDateTime.now(), null));
    }
}