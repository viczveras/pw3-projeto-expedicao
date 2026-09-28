package br.edu.ifpb.pweb3.turmalina.dominio;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoRelatorio;

class RelatorioFinalTest {

    @Test
    void deveCriarRelatorioEAprovarComPublicacao() {
        LocalDate dataSubmissao = LocalDate.of(2026, 8, 15);
        byte[] pdfMock = "%PDF-1.7 MOCK".getBytes();

        RelatorioFinal relatorio = new RelatorioFinal("Relatório de Campo", "Resumo das coletas realizadas",
                dataSubmissao, 45, pdfMock);

        assertEquals(SituacaoRelatorio.SUBMETIDO, relatorio.getSituacaoAprovacao());
        assertFalse(relatorio.isPublicacaoAutorizada());

        relatorio.aprovar(true);
        assertEquals(SituacaoRelatorio.APROVADO, relatorio.getSituacaoAprovacao());
        assertTrue(relatorio.isPublicacaoAutorizada());
    }

    @Test
    void deveReprovarRelatorio() {
        LocalDate dataSubmissao = LocalDate.of(2026, 8, 15);
        byte[] pdfMock = "%PDF-1.7 MOCK".getBytes();

        RelatorioFinal relatorio = new RelatorioFinal("Relatório Incompleto", "Resumo preliminar",
                dataSubmissao, 10, pdfMock);

        relatorio.reprovar();
        assertEquals(SituacaoRelatorio.REPROVADO, relatorio.getSituacaoAprovacao());
        assertFalse(relatorio.isPublicacaoAutorizada());
    }
}