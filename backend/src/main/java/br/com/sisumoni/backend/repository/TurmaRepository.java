package br.com.sisumoni.backend.repository;

import br.com.sisumoni.backend.domain.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, UUID> {
    boolean existsByNome(String nome);
    boolean existsByIdNot(UUID id);
}