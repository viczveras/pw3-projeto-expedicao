package br.edu.ifpb.pweb3.turmalina.consulta.dto;

import java.math.BigDecimal;

public record ResumoCaverna(String caverna, Long expedicoes, Long concluidas, BigDecimal orcamentoTotal,
                            BigDecimal custoTotal, BigDecimal percentualExecutado) {
}
