package br.com.sisumoni.backend.service;

import br.com.sisumoni.backend.domain.ConfiguracaoSistema;
import br.com.sisumoni.backend.repository.ConfiguracaoSistemaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracaoService {

    private final ConfiguracaoSistemaRepository configuracaoSistemaRepository;

    public ConfiguracaoService(ConfiguracaoSistemaRepository configuracaoSistemaRepository) {
        this.configuracaoSistemaRepository = configuracaoSistemaRepository;
    }

    @Transactional(readOnly = true)
    public ConfiguracaoSistema obter() {
        return configuracaoSistemaRepository.findById(1)
                .orElseThrow(() -> new IllegalStateException("Configuração do sistema não encontrada"));
    }

    @Transactional(readOnly = true)
    public boolean isCadastroEstudanteBloqueado() {
        return obter().isCadastroEstudanteBloqueado();
    }

    @Transactional
    public ConfiguracaoSistema atualizarCadastroEstudanteBloqueado(boolean bloqueado) {
        ConfiguracaoSistema configuracao = obter();
        configuracao.setCadastroEstudanteBloqueado(bloqueado);
        return configuracaoSistemaRepository.save(configuracao);
    }
}
