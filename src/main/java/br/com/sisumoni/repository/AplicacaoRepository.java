package br.com.sisumoni.repository;

import br.com.sisumoni.model.Aplicacao;
import br.com.sisumoni.model.enums.StatusAplicacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AplicacaoRepository extends JpaRepository<Aplicacao, Long> {
    List<Aplicacao> findByEstudanteId(Long estudanteId);
    List<Aplicacao> findByVagaId(Long vagaId);
    List<Aplicacao> findByStatus(StatusAplicacao status);
    List<Aplicacao> findByVagaIdAndStatus(Long vagaId, StatusAplicacao status);
}
