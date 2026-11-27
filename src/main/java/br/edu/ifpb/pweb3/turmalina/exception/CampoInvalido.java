package br.edu.ifpb.pweb3.turmalina.exception;

/**
 * Um campo rejeitado na requisição e o motivo.
 */
public record CampoInvalido(String campo, String mensagem) {
}
