package br.edu.ifpb.pweb3.turmalina.app;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import br.edu.ifpb.pweb3.turmalina.dominio.Equipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.TipoEquipamento;

class EquipamentoIT extends IntegracaoPostgres {

    @Test
    void deveSalvarERecuperarEquipamentoComPrecisaoDeValor() throws Exception {
        Equipamento equipamento = new Equipamento(
            "PAT-100",
            "Lanterna Principal",
            TipoEquipamento.ILUMINACAO,
            "Garmin",
            new BigDecimal("2500.50"),
            LocalDate.of(2025, 5, 10),
            false
        );

        transacao(em -> {
            em.persist(equipamento);
            return null;
        });

        Equipamento encontrado = transacao(em -> em.find(Equipamento.class, equipamento.getId()));

        assertNotNull(encontrado);
        assertEquals("PAT-100", encontrado.getCodigoPatrimonial());
        assertEquals(0, new BigDecimal("2500.50").compareTo(encontrado.getValorAquisicao()));
        assertEquals("numeric", tipoDaColuna("equipamento", "valor_aquisicao"));
        assertEquals("date", tipoDaColuna("equipamento", "data_compra"));
        assertEquals("boolean", tipoDaColuna("equipamento", "exige_calibracao"));
    }

    @Test
    void deveRejeitarCodigoPatrimonialDuplicado() {
        Equipamento eq1 = new Equipamento(
            "PAT-DUP",
            "Lanterna 1",
            TipoEquipamento.ILUMINACAO,
            "Petzl",
            new BigDecimal("80.00"),
            LocalDate.now(),
            false
        );

        Equipamento eq2 = new Equipamento(
            "PAT-DUP",
            "Lanterna 2",
            TipoEquipamento.ILUMINACAO,
            "Petzl",
            new BigDecimal("90.00"),
            LocalDate.now(),
            false
        );

        transacao(em -> {
            em.persist(eq1);
            return null;
        });

        Exception ex = assertThrows(Exception.class, () -> transacao(em -> {
            em.persist(eq2);
            return null;
        }));

        exigirSqlState(ex, "23505");
    }

    @Test
    void deveRejeitarValorAquisicaoNegativo() {
        Equipamento equipamento = new Equipamento(
            "PAT-NEG",
            "Lanterna 3",
            TipoEquipamento.ILUMINACAO,
            "Singing Rock",
            new BigDecimal("-50.00"),
            LocalDate.now(),
            false
        );

        Exception ex = assertThrows(Exception.class, () -> transacao(em -> {
            em.persist(equipamento);
            return null;
        }));

        exigirSqlState(ex, "23514");
    }
}