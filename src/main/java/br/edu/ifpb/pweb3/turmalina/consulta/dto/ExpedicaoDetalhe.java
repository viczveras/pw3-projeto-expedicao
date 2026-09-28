package br.edu.ifpb.pweb3.turmalina.consulta.dto;

import br.edu.ifpb.pweb3.turmalina.dominio.Expedicao;

import java.util.List;

public record ExpedicaoDetalhe(Expedicao expedicao, List<ParticipanteResumo> participantes) {
}
