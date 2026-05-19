package br.com.sisumoni.backend.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "turma")
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    public Turma() {}

    public Turma(String nome) {
        this.id = UUID.randomUUID();
        this.nome = nome;
    }
 
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
