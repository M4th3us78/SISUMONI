package br.com.sisumoni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.sisumoni.backend.domain.Usuario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByPerfil(Usuario.Perfil perfil);
    List<Usuario> findByPerfil(Usuario.Perfil perfil);
    
}
