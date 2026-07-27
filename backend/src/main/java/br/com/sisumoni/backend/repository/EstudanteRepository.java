package br.com.sisumoni.backend.repository;

import br.com.sisumoni.backend.domain.Estudante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstudanteRepository extends JpaRepository<Estudante, UUID> {
    boolean existsByMatricula(String matricula);
    List<Estudante> findByTurmaId(UUID turmaId);
    boolean existsByTurmaId(UUID turmaId);
    boolean existsByOpcao1IdOrOpcao2Id(UUID opcao1Id, UUID opcao2Id);

    String RELACIONAMENTOS = """
            JOIN FETCH e.turma
            LEFT JOIN FETCH e.opcao1 op1
            LEFT JOIN FETCH op1.departamento
            LEFT JOIN FETCH op1.turmas
            LEFT JOIN FETCH e.opcao2 op2
            LEFT JOIN FETCH op2.departamento
            LEFT JOIN FETCH op2.turmas
            """;

    @Query("SELECT DISTINCT e FROM Estudante e " + RELACIONAMENTOS)
    List<Estudante> findAllComRelacionamentos();

    @Query("SELECT DISTINCT e FROM Estudante e " + RELACIONAMENTOS + " WHERE e.turma.id = :turmaId")
    List<Estudante> findByTurmaIdComRelacionamentos(UUID turmaId);

    @Query("SELECT e FROM Estudante e " + RELACIONAMENTOS + " WHERE e.id = :id")
    Optional<Estudante> findByIdComRelacionamentos(UUID id);
}