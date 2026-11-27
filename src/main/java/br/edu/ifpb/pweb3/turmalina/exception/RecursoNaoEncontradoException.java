package br.edu.ifpb.pweb3.turmalina.exception;

/**
 * O recurso pedido não existe. Vira 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
