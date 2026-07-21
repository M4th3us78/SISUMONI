package br.com.sisumoni.backend.service;

import br.com.sisumoni.backend.domain.Departamento;
import br.com.sisumoni.backend.exception.RecursoNaoEncontradoException;
import br.com.sisumoni.backend.exception.RegraDeNegocioException;
import br.com.sisumoni.backend.repository.DepartamentoRepository;
import br.com.sisumoni.backend.repository.VagaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;
    private final VagaRepository vagaRepository;

    public DepartamentoService(DepartamentoRepository departamentoRepository, VagaRepository vagaRepository) {
        this.departamentoRepository = departamentoRepository;
        this.vagaRepository = vagaRepository;
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
        if (vagaRepository.existsByDepartamentoId(id)) {
            throw new RegraDeNegocioException("Não é possível deletar o departamento, pois existem vagas associadas a ele.");
        }
        departamentoRepository.delete(departamento);
    }
}
