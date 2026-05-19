// RecursoNaoEncontradoException.java
package br.com.sisumoni.backend.exception;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}