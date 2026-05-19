// RegraDeNegocioException.java
package br.com.sisumoni.backend.exception;

public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}