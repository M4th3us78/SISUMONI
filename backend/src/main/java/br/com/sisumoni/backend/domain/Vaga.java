package br.com.sisumoni.backend.domain;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "vaga")
public class Vaga {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false , length = 100)
    private String disciplina;

    @Column(nullable = false , length = 200)
    private String professor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;

    @Column(nullable = false)
    private Integer qtdBolsistas;

    @Column(nullable = false)
    private Integer qtdVoluntarios;

    @Column(nullable = false)
    private Integer qtdListaEspera;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "vaga_turma",
        joinColumns = @JoinColumn(name = "vaga_id"),
        inverseJoinColumns = @JoinColumn(name = "turma_id")
    )
    private Set<Turma> turmas = new HashSet<>();

    public Vaga() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getDisciplina() { return disciplina; }
    public void setDisciplina(String disciplina) { this.disciplina = disciplina; }
    public String getProfessor() { return professor; }
    public void setProfessor(String professor) { this.professor = professor; }
    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }
    public int getQtdBolsistas() { return qtdBolsistas; }
    public void setQtdBolsistas(int qtdBolsistas) { this.qtdBolsistas = qtdBolsistas; }
    public int getQtdVoluntarios() { return qtdVoluntarios; }
    public void setQtdVoluntarios(int qtdVoluntarios) { this.qtdVoluntarios = qtdVoluntarios; }
    public int getQtdListaEspera() { return qtdListaEspera; }
    public void setQtdListaEspera(int qtdListaEspera) { this.qtdListaEspera = qtdListaEspera; }
    public Set<Turma> getTurmas() { return turmas; }
    public void setTurmas(Set<Turma> turmas) { this.turmas = turmas; }
}
