package br.com.sisumoni.model.enums;

public enum StatusVaga {
    ABERTA("Vaga aberta para candidaturas"),
    FECHADA("Vaga fechada"),
    PREENCHIDA("Vaga preenchida"),
    CANCELADA("Vaga cancelada");

    private final String descricao;

    StatusVaga(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
