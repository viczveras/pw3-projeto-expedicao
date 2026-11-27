package br.edu.ifpb.pweb3.turmalina.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.time.Instant;
import java.util.List;

/**
 * Corpo JSON único de todas as respostas de erro da API, inclusive 401 e 403.
 *
 * @param instante momento do erro
 * @param status   código HTTP
 * @param erro     frase padrão do código HTTP (ex.: "Not Found")
 * @param mensagem explicação para quem consome a API
 * @param caminho  caminho da requisição
 * @param campos   campos rejeitados; vazio quando o erro não é de validação
 */
public record ErroResposta(Instant instante, int status, String erro, String mensagem, String caminho,
                           List<CampoInvalido> campos) {

    public ErroResposta {
        campos = campos == null ? List.of() : List.copyOf(campos);
    }

    public static ErroResposta de(HttpStatusCode status, String mensagem, String caminho) {
        return de(status, mensagem, caminho, List.of());
    }

    public static ErroResposta de(HttpStatusCode status, String mensagem, String caminho,
                                  List<CampoInvalido> campos) {
        HttpStatus conhecido = HttpStatus.resolve(status.value());
        String erro = conhecido != null ? conhecido.getReasonPhrase() : String.valueOf(status.value());
        return new ErroResposta(Instant.now(), status.value(), erro, mensagem, caminho, campos);
    }
}
