package br.edu.ifpb.pweb3.turmalina.consulta.dto;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.PapelParticipante;

import java.time.LocalDate;

public record ParticipanteResumo(Long participacaoId, Long pessoaId, String nome, PapelParticipante papel,
                                 LocalDate dataConfirmacao, boolean presencaConfirmada) {
}
