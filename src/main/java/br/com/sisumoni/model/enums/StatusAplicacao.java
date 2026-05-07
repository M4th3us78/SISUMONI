package br.com.sisumoni.model.enums;

public enum StatusAplicacao {
    PENDENTE("Pendente de análise"),
    APROVADA("Aprovada"),
    REJEITADA("Rejeitada"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusAplicacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
