package br.com.sisumoni.model;

import br.com.sisumoni.model.enums.StatusVaga;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "vagas_monitoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VagaMonitoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String disciplina;

    /**
     * Número de vagas para monitoria voluntária (sem bolsa).
     */
    @Column(name = "quantidade_vagas_voluntarias")
    private Integer quantidadeVagasVoluntarias = 0;

    /**
     * Número de vagas para monitoria com bolsa.
     */
    @Column(name = "quantidade_vagas_bolsistas")
    private Integer quantidadeVagasBolsistas = 0;

    /**
     * Número total de vagas efetivamente preenchidas (bolsistas + voluntárias).
     */
    @Column(name = "vagas_preenchidas")
    private Integer vagasPreenchidas = 0;

    /**
     * Número de vagas disponíveis na lista de espera.
     */
    @Column(name = "quantidade_lista_espera")
    private Integer quantidadeListaEspera = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusVaga status = StatusVaga.ABERTA;

    @JoinColumn(nullable = true)
    private String professor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime dataAtualizacao = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}
