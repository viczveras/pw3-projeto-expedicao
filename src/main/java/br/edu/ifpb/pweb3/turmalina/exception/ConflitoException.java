package br.edu.ifpb.pweb3.turmalina.exception;

/**
 * A operação conflita com o estado atual dos dados (ex.: código já cadastrado). Vira 409.
 */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
