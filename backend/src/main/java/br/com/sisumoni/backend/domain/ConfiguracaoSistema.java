package br.com.sisumoni.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "configuracao_sistema")
public class ConfiguracaoSistema {

    @Id
    private Integer id;

    @Column(name = "cadastro_estudante_bloqueado", nullable = false)
    private boolean cadastroEstudanteBloqueado;

    public ConfiguracaoSistema() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public boolean isCadastroEstudanteBloqueado() { return cadastroEstudanteBloqueado; }
    public void setCadastroEstudanteBloqueado(boolean cadastroEstudanteBloqueado) { this.cadastroEstudanteBloqueado = cadastroEstudanteBloqueado; }
}
