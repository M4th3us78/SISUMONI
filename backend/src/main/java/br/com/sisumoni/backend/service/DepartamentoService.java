package br.com.sisumoni.backend.service;

import br.com.sisumoni.backend.domain.Departamento;
import br.com.sisumoni.backend.exception.RecursoNaoEncontradoException;
import br.com.sisumoni.backend.exception.RegraDeNegocioException;
import br.com.sisumoni.backend.repository.DepartamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DepartamentoService {
    
    private final DepartamentoRepository departamentoRepository;

    public DepartamentoService(DepartamentoRepository departamentoRepository) {
        this.departamentoRepository = departamentoRepository;
    }

    public List<Departamento> listar() {
        return departamentoRepository.findAll();
    }

    public Departamento buscarPorId(UUID id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Departamento não encontrado com id: " + id));
    }

    public Departamento cadastrar(String nome) {
        if (departamentoRepository.existsByNome(nome)) {
            throw new RegraDeNegocioException("Já existe um departamento com esse nome: " + nome);
        }
        Departamento departamento = new Departamento();
        departamento.setNome(nome);
        return departamentoRepository.save(departamento);
    }

    public void deletar(UUID id) {
        Departamento departamento = buscarPorId(id);
        //Adiconar verificação se existe vagas vinculadas
        departamentoRepository.delete(departamento);
    }
}
