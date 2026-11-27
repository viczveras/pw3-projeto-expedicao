package br.edu.ifpb.pweb3.turmalina.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converte as exceções lançadas pelos controladores no corpo {@link ErroResposta}.
 *
 * <p>Atenção ao integrar o Spring Security: o handler genérico de {@link Exception} captura também a
 * {@code AccessDeniedException} lançada pelo {@code @PreAuthorize}, o que transformaria o 403 em 500.
 * O PR de segurança deve acrescentar aqui um handler específico para {@code AccessDeniedException}
 * (403) e {@code AuthenticationException} (401), que o Spring escolhe antes do genérico.</p>
 */
@RestControllerAdvice
public class TratadorGlobalDeErros {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorGlobalDeErros.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<ErroResposta> recursoNaoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest requisicao) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage(), requisicao);
    }

    @ExceptionHandler(ConflitoException.class)
    ResponseEntity<ErroResposta> conflito(ConflitoException e, HttpServletRequest requisicao) {
        return responder(HttpStatus.CONFLICT, e.getMessage(), requisicao);
    }

    @ExceptionHandler(ArquivoInvalidoException.class)
    ResponseEntity<ErroResposta> arquivoInvalido(ArquivoInvalidoException e, HttpServletRequest requisicao) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage(), requisicao);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException e, HttpServletRequest requisicao) {
        Stream<CampoInvalido> deCampo = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> new CampoInvalido(erro.getField(), erro.getDefaultMessage()));
        Stream<CampoInvalido> deObjeto = e.getBindingResult().getGlobalErrors().stream()
                .map(erro -> new CampoInvalido(erro.getObjectName(), erro.getDefaultMessage()));
        List<CampoInvalido> campos = Stream.concat(deCampo, deObjeto).toList();
        return responder(HttpStatus.BAD_REQUEST, "Há campos inválidos na requisição.", requisicao, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErroResposta> corpoIlegivel(HttpMessageNotReadableException e, HttpServletRequest requisicao) {
        if (e.getCause() instanceof MismatchedInputException entrada && !entrada.getPath().isEmpty()) {
            CampoInvalido campo = new CampoInvalido(caminhoDoCampo(entrada), motivo(entrada));
            return responder(HttpStatus.BAD_REQUEST, "Há campos inválidos na requisição.", requisicao, List.of(campo));
        }
        return responder(HttpStatus.BAD_REQUEST, "O corpo da requisição está ausente ou não é um JSON válido.",
                requisicao);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErroResposta> tipoDoParametro(MethodArgumentTypeMismatchException e, HttpServletRequest requisicao) {
        String mensagem = "O parâmetro '%s' recebeu um valor inválido: '%s'.".formatted(e.getName(), e.getValue());
        return responder(HttpStatus.BAD_REQUEST, mensagem, requisicao);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    ResponseEntity<ErroResposta> parametroAusente(Exception e, HttpServletRequest requisicao) {
        String nome = e instanceof MissingServletRequestParameterException p ? p.getParameterName()
                : ((MissingServletRequestPartException) e).getRequestPartName();
        return responder(HttpStatus.BAD_REQUEST, "O parâmetro obrigatório '%s' não foi enviado.".formatted(nome),
                requisicao);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErroResposta> argumentoInvalido(IllegalArgumentException e, HttpServletRequest requisicao) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage(), requisicao);
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ErroResposta> estadoInvalido(IllegalStateException e, HttpServletRequest requisicao) {
        return responder(HttpStatus.CONFLICT, e.getMessage(), requisicao);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErroResposta> integridade(DataIntegrityViolationException e, HttpServletRequest requisicao) {
        LOG.warn("Violação de integridade em {}: {}", requisicao.getRequestURI(), e.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT,
                "A operação viola uma restrição dos dados: registro duplicado ou ainda referenciado por outro.",
                requisicao);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ErroResposta> arquivoGrande(MaxUploadSizeExceededException e, HttpServletRequest requisicao) {
        return responder(HttpStatus.PAYLOAD_TOO_LARGE, "O arquivo excede o tamanho máximo permitido.", requisicao);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ErroResposta> midiaNaoSuportada(HttpMediaTypeNotSupportedException e, HttpServletRequest requisicao) {
        String mensagem = "Tipo de conteúdo não suportado: %s.".formatted(e.getContentType());
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE, mensagem, requisicao);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    ResponseEntity<Void> formatoNaoAceito() {
        // O cliente não aceita JSON, então a resposta vai sem corpo (um ErroResposta também não seria aceito).
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErroResposta> metodoNaoSuportado(HttpRequestMethodNotSupportedException e,
                                                    HttpServletRequest requisicao) {
        String mensagem = "O método %s não é aceito neste caminho.".formatted(e.getMethod());
        return responder(HttpStatus.METHOD_NOT_ALLOWED, mensagem, requisicao);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErroResposta> caminhoInexistente(NoResourceFoundException e, HttpServletRequest requisicao) {
        return responder(HttpStatus.NOT_FOUND, "Caminho não encontrado.", requisicao);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErroResposta> inesperado(Exception e, HttpServletRequest requisicao) {
        if (e instanceof ErrorResponse respostaDoSpring) {
            // Demais exceções do Spring MVC já trazem o status certo; não são erro do servidor.
            HttpStatusCode status = respostaDoSpring.getStatusCode();
            return responder(status, "A requisição não pôde ser atendida.", requisicao);
        }
        LOG.error("Erro inesperado em {} {}", requisicao.getMethod(), requisicao.getRequestURI(), e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor.", requisicao);
    }

    private static ResponseEntity<ErroResposta> responder(HttpStatusCode status, String mensagem,
                                                          HttpServletRequest requisicao) {
        return responder(status, mensagem, requisicao, List.of());
    }

    private static ResponseEntity<ErroResposta> responder(HttpStatusCode status, String mensagem,
                                                          HttpServletRequest requisicao, List<CampoInvalido> campos) {
        return ResponseEntity.status(status)
                .body(ErroResposta.de(status, mensagem, requisicao.getRequestURI(), campos));
    }

    private static String caminhoDoCampo(MismatchedInputException e) {
        return e.getPath().stream()
                .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                .collect(Collectors.joining("."))
                .replace(".[", "[");
    }

    private static String motivo(JsonMappingException e) {
        if (e instanceof InvalidFormatException formato && formato.getTargetType() != null
                && formato.getTargetType().isEnum()) {
            String aceitos = Arrays.stream(formato.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            return "Valor inválido. Valores aceitos: " + aceitos + ".";
        }
        return "Valor inválido ou de tipo incompatível.";
    }
}
