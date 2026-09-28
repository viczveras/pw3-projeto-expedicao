package br.edu.ifpb.pweb3.turmalina.dominio;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.DatumGeodesico;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.EstadoEquipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.TipoEquipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Endereco;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Localizacao;

class MovimentacaoEquipamentoTest {

    private final Instant retiradaFixa = Instant.parse("2026-08-10T08:00:00Z");
    private final Instant previsaoFixa = Instant.parse("2026-08-15T18:00:00Z");

    private Pessoa criarPessoaValida() {
        Endereco endereco = new Endereco("Rua A", "10", null, "Centro", "João Pessoa", UnidadeFederativa.PB, "58000-000");
        return new Pessoa("Carlos", "333.444.555-66", LocalDate.of(1992, 5, 15), "carlos@ifpb.edu.br", "(83) 97777-2222", endereco);
    }

    private Equipamento criarEquipamentoValido() {
        return new Equipamento("PAT-500", "Detector Multigás", TipoEquipamento.ILUMINACAO, 
                "MSA", new BigDecimal("3000.00"), LocalDate.of(2026, 1, 10), false);
    }

    private Expedicao criarExpedicaoValida() {
        Caverna caverna = new Caverna("Caverna B", "CB-01", "Rocha", UnidadeFederativa.PB, 
                new Localizacao(new BigDecimal("-7.12"), new BigDecimal("-34.88"), DatumGeodesico.SIRGAS_2000));
        PlanoSeguranca plano = new PlanoSeguranca("Resgate manual", "Base", 120, "193", true);
        return new Expedicao("EXP-03", "Expedição C", "Análise", caverna, 
                LocalDateTime.of(2026, 8, 10, 8, 0), LocalDateTime.of(2026, 8, 20, 18, 0), new BigDecimal("8000.00"), 5, plano);
    }

    @Test
    void deveRegistrarDevolucaoComSucesso() {
        Expedicao expedicao = criarExpedicaoValida();
        Equipamento equipamento = criarEquipamentoValido();
        Pessoa pessoa = criarPessoaValida();

        MovimentacaoEquipamento mov = new MovimentacaoEquipamento(
                expedicao, equipamento, pessoa, retiradaFixa, previsaoFixa, EstadoEquipamento.BOM);

        assertFalse(mov.isDevolvido());

        Instant devolucaoFixa = Instant.parse("2026-08-14T15:00:00Z");
        mov.registrarDevolucao(devolucaoFixa, EstadoEquipamento.DANIFICADO, new BigDecimal("150.00"));

        assertTrue(mov.isDevolvido());
        assertEquals(devolucaoFixa, mov.getDevolucaoEfetiva());
        assertEquals(EstadoEquipamento.DANIFICADO, mov.getEstadoRetorno());
        assertEquals(new BigDecimal("150.00"), mov.getCustoAvaria());
    }

    @Test
    void deveRejeitarDevolucaoAnteriorARetirada() {
        Expedicao expedicao = criarExpedicaoValida();
        Equipamento equipamento = criarEquipamentoValido();
        Pessoa pessoa = criarPessoaValida();

        MovimentacaoEquipamento mov = new MovimentacaoEquipamento(
                expedicao, equipamento, pessoa, retiradaFixa, previsaoFixa, EstadoEquipamento.BOM);

        Instant devolucaoInvalida = Instant.parse("2026-08-09T08:00:00Z");

        assertThrows(IllegalArgumentException.class, () -> 
                mov.registrarDevolucao(devolucaoInvalida, EstadoEquipamento.BOM, BigDecimal.ZERO));
    }
}