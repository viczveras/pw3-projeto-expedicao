package br.edu.ifpb.pweb3.turmalina.dominio;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

    private Pessoa criarPessoaValida() {
        Endereco endereco = new Endereco("Rua A", "10", null, "Centro", "João Pessoa", UnidadeFederativa.PB, "58000-000");
        return new Pessoa("Carlos", "333.444.555-66", LocalDate.of(1992, 5, 15), "carlos@ifpb.edu.br", "(83) 97777-2222", endereco);
    }

    private Equipamento criarEquipamentoValido() {
        return new Equipamento("PAT-500", "Detector Multigás", TipoEquipamento.ILUMINACAO, 
                "MSA", new BigDecimal("3000.00"), LocalDate.now().minusMonths(3), false);
    }

    private Expedicao criarExpedicaoValida() {
        Caverna caverna = new Caverna("Caverna B", "CB-01", "Rocha", UnidadeFederativa.PB, 
                new Localizacao(new BigDecimal("-7.12"), new BigDecimal("-34.88"), DatumGeodesico.SIRGAS_2000));
        PlanoSeguranca plano = new PlanoSeguranca("Resgate manual", "Base", 120, "193", true);
        return new Expedicao("EXP-03", "Expedição C", "Análise", caverna, 
                LocalDateTime.now(), LocalDateTime.now().plusDays(10), new BigDecimal("8000.00"), 5, plano);
    }

    @Test
    void deveRegistrarDevolucaoComSucesso() {
        Expedicao expedicao = criarExpedicaoValida();
        Equipamento equipamento = criarEquipamentoValido();
        Pessoa pessoa = criarPessoaValida();

        Instant agora = Instant.now();
        Instant previsao = agora.plus(5, ChronoUnit.DAYS);

        MovimentacaoEquipamento mov = new MovimentacaoEquipamento(
                expedicao, equipamento, pessoa, agora, previsao, EstadoEquipamento.BOM);

        assertFalse(mov.isDevolvido());

        Instant devolucao = agora.plus(4, ChronoUnit.DAYS);
        mov.registrarDevolucao(devolucao, EstadoEquipamento.DANIFICADO, new BigDecimal("150.00"));

        assertTrue(mov.isDevolvido());
        assertEquals(devolucao, mov.getDevolucaoEfetiva());
        assertEquals(EstadoEquipamento.DANIFICADO, mov.getEstadoRetorno());
        assertEquals(new BigDecimal("150.00"), mov.getCustoAvaria());
    }

    @Test
    void deveRejeitarDevolucaoAnteriorARetirada() {
        Expedicao expedicao = criarExpedicaoValida();
        Equipamento equipamento = criarEquipamentoValido();
        Pessoa pessoa = criarPessoaValida();

        Instant agora = Instant.now();
        Instant previsao = agora.plus(5, ChronoUnit.DAYS);

        MovimentacaoEquipamento mov = new MovimentacaoEquipamento(
                expedicao, equipamento, pessoa, agora, previsao, EstadoEquipamento.BOM);

        Instant devolucaoInvalida = agora.minus(1, ChronoUnit.DAYS);

        assertThrows(IllegalArgumentException.class, () -> 
                mov.registrarDevolucao(devolucaoInvalida, EstadoEquipamento.BOM, BigDecimal.ZERO));
    }
}