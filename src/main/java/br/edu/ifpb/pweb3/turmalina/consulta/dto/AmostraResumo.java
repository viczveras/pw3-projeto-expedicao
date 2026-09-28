package br.edu.ifpb.pweb3.turmalina.consulta.dto;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.CategoriaAmostra;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.CondicaoConservacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeMedida;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AmostraResumo(Long id, String codigoCampo, CategoriaAmostra categoria, BigDecimal quantidade,
                            UnidadeMedida unidadeMedida, LocalDate dataAcondicionamento,
                            CondicaoConservacao condicaoConservacao, boolean materialPerigoso,
                            String observacoes) {
}
