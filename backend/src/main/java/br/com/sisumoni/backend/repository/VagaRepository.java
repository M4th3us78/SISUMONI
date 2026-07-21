package br.com.sisumoni.backend.repository;

import br.com.sisumoni.backend.domain.Vaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VagaRepository extends JpaRepository<Vaga, UUID> {
    boolean existsByDepartamentoId(UUID departamentoId);

    @Query("SELECT DISTINCT v FROM Vaga v JOIN FETCH v.departamento LEFT JOIN FETCH v.turmas")
    List<Vaga> findAllComRelacionamentos();

    @Query("SELECT v FROM Vaga v JOIN FETCH v.departamento LEFT JOIN FETCH v.turmas WHERE v.id = :id")
    Optional<Vaga> findByIdComRelacionamentos(UUID id);
}