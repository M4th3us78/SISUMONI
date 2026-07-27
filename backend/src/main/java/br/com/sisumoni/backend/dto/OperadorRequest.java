package br.com.sisumoni.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;


public record OperadorRequest(
    @NotBlank(message = "O nome é obrigatório")
    String nome,

    @Email(message = "O E-mail inválido")
    @NotBlank(message = "O E-mail é obrigatório")
    String email,

    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    @NotBlank(message = "A senha é obrigatória")
    String senha,

    @NotEmpty(message = "Selecione ao menos uma Turma")
    Set<UUID> turmasIds 
){}
