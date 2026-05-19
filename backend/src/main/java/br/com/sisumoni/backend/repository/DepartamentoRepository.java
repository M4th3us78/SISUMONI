package br.com.sisumoni.backend.repository;

import br.com.sisumoni.backend.domain.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, UUID> {
    boolean existsByNome(String nome);
    //boolean existsByVagasIsNotEmpty(UUID id);
}