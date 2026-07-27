package br.com.sisumoni.backend.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "classificacao")
public class Classificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudante_id", nullable = false)
    private Estudante estudante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_id", nullable = false)
    private Vaga vaga;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal pontuacao;

    @Column
    private Integer posicao;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Tipo tipo;

    @Column(nullable = false)
    private boolean empate = false;

    public enum Tipo {
        BOLSISTA, VOLUNTARIO, LISTA_ESPERA
    }

    public Classificacao() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Estudante getEstudante() { return estudante; }
    public void setEstudante(Estudante estudante) { this.estudante = estudante; }
    public Vaga getVaga() { return vaga; }
    public void setVaga(Vaga vaga) { this.vaga = vaga; }
    public BigDecimal getPontuacao() { return pontuacao; }
    public void setPontuacao(BigDecimal pontuacao) { this.pontuacao = pontuacao; }
    public Integer getPosicao() { return posicao; }
    public void setPosicao(Integer posicao) { this.posicao = posicao; }
    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
    public boolean isEmpate() { return empate; }
    public void setEmpate(boolean empate) { this.empate = empate; }
}