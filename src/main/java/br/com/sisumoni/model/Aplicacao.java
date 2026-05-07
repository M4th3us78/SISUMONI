package br.com.sisumoni.model;

import br.com.sisumoni.model.enums.StatusAplicacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "aplicacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aplicacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "estudante_id", nullable = false)
    private Estudante estudante;

    @ManyToOne
    @JoinColumn(name = "vaga_id", nullable = false)
    private VagaMonitoria vaga;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAplicacao status = StatusAplicacao.PENDENTE;

    @Column(name = "primeira_opcao")
    private Boolean primeiraOpcao = false;

    @Column(name = "segunda_opcao")
    private Boolean segundaOpcao = false;

    @Column(name = "media_materia", nullable = false)
    private Double mediaMateria;

    @Column(name = "data_candidatura", nullable = false, updatable = false)
    private LocalDateTime dataCandidatura = LocalDateTime.now();

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}
