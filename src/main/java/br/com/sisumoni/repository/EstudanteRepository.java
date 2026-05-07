package br.com.sisumoni.repository;

import br.com.sisumoni.model.Estudante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EstudanteRepository extends JpaRepository<Estudante, Long> {
    Optional<Estudante> findByMatricula(String matricula);
    Optional<Estudante> findByEmail(String email);
}
