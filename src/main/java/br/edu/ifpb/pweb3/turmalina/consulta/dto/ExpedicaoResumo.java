package br.edu.ifpb.pweb3.turmalina.consulta.dto;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoExpedicao;

import java.time.LocalDateTime;

public record ExpedicaoResumo(Long id, String codigo, String titulo, String caverna,
                              LocalDateTime inicioPrevisto, LocalDateTime terminoPrevisto,
                              SituacaoExpedicao situacao) {
}
