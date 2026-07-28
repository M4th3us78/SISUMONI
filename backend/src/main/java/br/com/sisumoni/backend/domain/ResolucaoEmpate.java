package br.com.sisumoni.backend.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "resolucao_empate")
public class ResolucaoEmpate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_id", nullable = false)
    private Vaga vaga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudante_vencedor", nullable = false)
    private Estudante vencedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudante_perdedor", nullable = false)
    private Estudante perdedor;

    @Column(name = "pontuacao_empate", nullable = false, precision = 6, scale = 4)
    private BigDecimal pontuacaoEmpate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolvido_por")
    private Usuario resolvidoPor;

    @Column(name = "resolvido_em", nullable = false)
    private LocalDateTime resolvidoEm = LocalDateTime.now();

    public ResolucaoEmpate() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Vaga getVaga() { return vaga; }
    public void setVaga(Vaga vaga) { this.vaga = vaga; }
    public Estudante getVencedor() { return vencedor; }
    public void setVencedor(Estudante vencedor) { this.vencedor = vencedor; }
    public Estudante getPerdedor() { return perdedor; }
    public void setPerdedor(Estudante perdedor) { this.perdedor = perdedor; }
    public BigDecimal getPontuacaoEmpate() { return pontuacaoEmpate; }
    public void setPontuacaoEmpate(BigDecimal p) { this.pontuacaoEmpate = p; }
    public Usuario getResolvidoPor() { return resolvidoPor; }
    public void setResolvidoPor(Usuario u) { this.resolvidoPor = u; }
    public LocalDateTime getResolvidoEm() { return resolvidoEm; }
    public void setResolvidoEm(LocalDateTime d) { this.resolvidoEm = d; }
}
