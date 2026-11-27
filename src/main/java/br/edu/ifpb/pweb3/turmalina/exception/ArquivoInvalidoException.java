package br.edu.ifpb.pweb3.turmalina.exception;

/**
 * O arquivo enviado está vazio ou tem tipo não aceito. Vira 400.
 */
public class ArquivoInvalidoException extends RuntimeException {

    public ArquivoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
