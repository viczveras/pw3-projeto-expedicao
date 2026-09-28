package br.edu.ifpb.pweb3.turmalina.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.CategoriaAmostra;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.CondicaoConservacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeMedida;

class AmostraTest {

    @Test
    void deveValidarQuantidadePositivaEAtualizarFotografia() {
        LocalDate dataAcondicionamento = LocalDate.of(2026, 8, 11);
        Amostra amostra = new Amostra("AMS-100", CategoriaAmostra.SEDIMENTO, new BigDecimal("150.00"),
                UnidadeMedida.GRAMA, dataAcondicionamento, CondicaoConservacao.INTEGRA, false);

        assertEquals("AMS-100", amostra.getCodigoCampo());
        assertEquals(CondicaoConservacao.INTEGRA, amostra.getCondicaoConservacao());

        byte[] fotoMock = "JPEG_FOTO_MOCK".getBytes();
        amostra.setFotografia(fotoMock);
        assertArrayEquals(fotoMock, amostra.getFotografia());
    }

    @Test
    void deveRejeitarQuantidadeZeroOuNegativa() {
        LocalDate dataAcondicionamento = LocalDate.of(2026, 8, 11);

        assertThrows(IllegalArgumentException.class, () -> new Amostra("AMS-ERR", CategoriaAmostra.SEDIMENTO,
                BigDecimal.ZERO, UnidadeMedida.GRAMA, dataAcondicionamento, CondicaoConservacao.INTEGRA, false));

        assertThrows(IllegalArgumentException.class, () -> new Amostra("AMS-ERR", CategoriaAmostra.SEDIMENTO,
                new BigDecimal("-5.00"), UnidadeMedida.GRAMA, dataAcondicionamento, CondicaoConservacao.INTEGRA, false));
    }
}