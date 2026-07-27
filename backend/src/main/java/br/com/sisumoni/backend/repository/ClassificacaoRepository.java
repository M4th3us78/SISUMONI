package br.com.sisumoni.backend.repository;

import br.com.sisumoni.backend.domain.Classificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClassificacaoRepository extends JpaRepository<Classificacao, UUID> {

    // Classificações de uma vaga, já com estudante carregado, ordenadas por posição
    @Query("""
        SELECT c FROM Classificacao c
        JOIN FETCH c.estudante e
        WHERE c.vaga.id = :vagaId
        ORDER BY c.posicao ASC
        """)
    List<Classificacao> findByVagaIdComEstudante(@Param("vagaId") UUID vagaId);

    // Só os registros crus da vaga (para o recálculo comparar e atualizar)
    List<Classificacao> findByVagaId(UUID vagaId);

    boolean existsByVagaIdAndEmpateTrue(UUID vagaId);

    boolean existsByEmpateTrue();
}