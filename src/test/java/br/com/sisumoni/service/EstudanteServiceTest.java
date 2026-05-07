package br.com.sisumoni.service;

import br.com.sisumoni.model.Estudante;
import br.com.sisumoni.repository.EstudanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstudanteServiceTest {
    @Mock
    private EstudanteRepository estudanteRepository;

    @InjectMocks
    private EstudanteService estudanteService;

    private Estudante estudante;

    @BeforeEach
    void setup() {
        estudante = new Estudante();
        estudante.setId(1L);
        estudante.setMatricula("2022001");
        estudante.setNome("João Silva");
        estudante.setEmail("joao@example.com");
        estudante.setMediaGeral(8.5);
        estudante.setPeriodoAtual(4);
    }

    @Test
    void testCriarEstudante() {
        when(estudanteRepository.save(any(Estudante.class))).thenReturn(estudante);

        Estudante resultado = estudanteService.criarEstudante(estudante);

        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        assertEquals("2022001", resultado.getMatricula());
    }

    @Test
    void testObterEstudantePorMatricula() {
        when(estudanteRepository.findByMatricula("2022001")).thenReturn(Optional.of(estudante));

        Optional<Estudante> resultado = estudanteService.obterPorMatricula("2022001");

        assertTrue(resultado.isPresent());
        assertEquals("João Silva", resultado.get().getNome());
    }

    @Test
    void testObterEstudanteNaoEncontrado() {
        when(estudanteRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Estudante> resultado = estudanteService.obterEstudante(999L);

        assertFalse(resultado.isPresent());
    }
}
