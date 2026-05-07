package br.com.sisumoni.repository;

import br.com.sisumoni.model.VagaMonitoria;
import br.com.sisumoni.model.enums.StatusVaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VagaMonitoriaRepository extends JpaRepository<VagaMonitoria, Long> {
    List<VagaMonitoria> findByStatus(StatusVaga status);
    List<VagaMonitoria> findByProfessorId(Long professorId);
    List<VagaMonitoria> findByDisciplinaContainingIgnoreCase(String disciplina);
}
