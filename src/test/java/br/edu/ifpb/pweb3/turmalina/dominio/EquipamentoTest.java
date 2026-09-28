package br.edu.ifpb.pweb3.turmalina.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoOperacional;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.TipoEquipamento;

class EquipamentoTest {

    @Test
    void deveRegistrarManutencaoEAtualizarDataESituacao() {
        Equipamento equipamento = new Equipamento(
            "PAT-001",
            "Lanterna de Cabeça",
            TipoEquipamento.ILUMINACAO,
            "Petzl",
            new BigDecimal("150.00"),
            LocalDate.now().minusMonths(6),
            false
        );

        equipamento.setSituacaoOperacional(SituacaoOperacional.EM_MANUTENCAO);
        assertEquals(SituacaoOperacional.EM_MANUTENCAO, equipamento.getSituacaoOperacional());

        LocalDate hoje = LocalDate.now();
        equipamento.registrarManutencao(hoje);

        assertEquals(hoje, equipamento.getDataUltimaManutencao());
        assertEquals(SituacaoOperacional.DISPONIVEL, equipamento.getSituacaoOperacional());
    }

    @Test
    void deveCriarEquipamentoComAtributosValidos() {
        Equipamento equipamento = new Equipamento(
            "PAT-002",
            "Lanterna de Backup",
            TipoEquipamento.ILUMINACAO,
            "MSA",
            new BigDecimal("1200.00"),
            LocalDate.now().minusYears(1),
            true
        );

        assertEquals("PAT-002", equipamento.getCodigoPatrimonial());
        assertEquals("Lanterna de Backup", equipamento.getNome());
        assertEquals(TipoEquipamento.ILUMINACAO, equipamento.getTipo());
        assertEquals("MSA", equipamento.getFabricante());
        assertTrue(equipamento.isExigeCalibracao());
        assertEquals(SituacaoOperacional.DISPONIVEL, equipamento.getSituacaoOperacional());
    }
}