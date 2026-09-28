package br.edu.ifpb.pweb3.turmalina.app;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import br.edu.ifpb.pweb3.turmalina.dominio.Caverna;
import br.edu.ifpb.pweb3.turmalina.dominio.Equipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.Expedicao;
import br.edu.ifpb.pweb3.turmalina.dominio.MovimentacaoEquipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.Pessoa;
import br.edu.ifpb.pweb3.turmalina.dominio.PlanoSeguranca;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.DatumGeodesico;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.EstadoEquipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.TipoEquipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Endereco;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Localizacao;

class MovimentacaoEquipamentoIT extends IntegracaoPostgres {

    @Test
    void deveRegistrarMovimentacaoDevolucaoECustoAvaria() throws Exception {
        Equipamento equipamento = new Equipamento("PAT-500", "Detector Multigás", TipoEquipamento.ILUMINACAO, 
                "MSA", new BigDecimal("3000.00"), LocalDate.of(2026, 1, 10), false);

        Caverna caverna = new Caverna("Caverna B", "CB-01", "Rocha", UnidadeFederativa.PB, 
                new Localizacao(new BigDecimal("-7.12"), new BigDecimal("-34.88"), DatumGeodesico.SIRGAS_2000));

        PlanoSeguranca plano = new PlanoSeguranca("Resgate manual", "Base", 120, "193", true);
        Expedicao expedicao = new Expedicao("EXP-03", "Expedição C", "Análise", caverna, 
                LocalDateTime.of(2026, 8, 10, 8, 0), LocalDateTime.of(2026, 8, 20, 18, 0), new BigDecimal("8000.00"), 5, plano);

        Endereco endereco = new Endereco("Rua C", "30", null, "Centro", "João Pessoa", UnidadeFederativa.PB, "58000-000");
        Pessoa pessoa = new Pessoa("Carlos", "333.444.555-66", LocalDate.of(1992, 5, 15), 
                "carlos@ifpb.edu.br", "(83) 97777-2222", endereco);

        Instant retiradaFixa = Instant.parse("2026-08-10T08:00:00Z");
        Instant previsaoFixa = Instant.parse("2026-08-15T18:00:00Z");

        MovimentacaoEquipamento mov = new MovimentacaoEquipamento(expedicao, equipamento, pessoa, 
                retiradaFixa, previsaoFixa, EstadoEquipamento.BOM);

        transacao(em -> {
            em.persist(equipamento);
            em.persist(caverna);
            em.persist(expedicao);
            em.persist(pessoa);
            em.persist(mov);
            return null;
        });

        Instant devolucaoFixa = Instant.parse("2026-08-14T15:00:00Z");
        mov.registrarDevolucao(devolucaoFixa, EstadoEquipamento.DANIFICADO, new BigDecimal("150.00"));

        transacao(em -> {
            em.merge(mov);
            return null;
        });

        MovimentacaoEquipamento encontrada = transacao(em -> em.find(MovimentacaoEquipamento.class, mov.getId()));

        assertNotNull(encontrada);
        assertTrue(encontrada.isDevolvido());
        assertEquals(0, new BigDecimal("150.00").compareTo(encontrada.getCustoAvaria()));
        assertEquals("numeric", tipoDaColuna("movimentacao_equipamento", "custo_avaria"));
        assertEquals(retiradaFixa, encontrada.getRetiradaEm());
    }
}