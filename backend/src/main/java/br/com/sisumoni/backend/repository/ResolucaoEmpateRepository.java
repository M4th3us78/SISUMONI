package br.com.sisumoni.backend.repository;

import br.com.sisumoni.backend.domain.ResolucaoEmpate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResolucaoEmpateRepository extends JpaRepository<ResolucaoEmpate, UUID> {

    // Resoluções de uma vaga, com os dois estudantes carregados
    @Query("""
        SELECT r FROM ResolucaoEmpate r
        JOIN FETCH r.vencedor
        JOIN FETCH r.perdedor
        WHERE r.vaga.id = :vagaId
        """)
    List<ResolucaoEmpate> findByVagaIdComEstudantes(@Param("vagaId") UUID vagaId);

    // Todas as resoluções de uma vaga (para limpar quando reabrir)
    List<ResolucaoEmpate> findByVagaId(UUID vagaId);
}
