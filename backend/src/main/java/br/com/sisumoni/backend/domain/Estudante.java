package br.com.sisumoni.backend.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "estudante")
public class Estudante {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "nome_fantasia", nullable = false, length = 50)
    private String nomeFantasia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal ira;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcao1_id")
    private Vaga opcao1;

    @Column(name = "media_opcao1", precision = 4, scale = 1)
    private BigDecimal mediaOpcao1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcao2_id")
    private Vaga opcao2;

    @Column(name = "media_opcao2", precision = 4, scale = 1)
    private BigDecimal mediaOpcao2;

    public Estudante() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }
    public Turma getTurma() { return turma; }
    public void setTurma(Turma turma) { this.turma = turma; }
    public BigDecimal getIra() { return ira; }
    public void setIra(BigDecimal ira) { this.ira = ira; }
    public Vaga getOpcao1() { return opcao1; }
    public void setOpcao1(Vaga opcao1) { this.opcao1 = opcao1; }
    public BigDecimal getMediaOpcao1() { return mediaOpcao1; }
    public void setMediaOpcao1(BigDecimal mediaOpcao1) { this.mediaOpcao1 = mediaOpcao1; }
    public Vaga getOpcao2() { return opcao2; }
    public void setOpcao2(Vaga opcao2) { this.opcao2 = opcao2; }
    public BigDecimal getMediaOpcao2() { return mediaOpcao2; }
    public void setMediaOpcao2(BigDecimal mediaOpcao2) { this.mediaOpcao2 = mediaOpcao2; }
}