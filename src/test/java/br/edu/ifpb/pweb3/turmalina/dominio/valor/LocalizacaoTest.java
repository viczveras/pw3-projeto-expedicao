package br.edu.ifpb.pweb3.turmalina.dominio.valor;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.DatumGeodesico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalizacaoTest {

    @ParameterizedTest
    @CsvSource({"90,180", "-90,-180", "90,-180", "-90,180"})
    void aceitaLimitesGeograficos(String latitude, String longitude) {
        Localizacao localizacao = criar(latitude, longitude);

        assertEquals(new BigDecimal(latitude), localizacao.getLatitude());
        assertEquals(new BigDecimal(longitude), localizacao.getLongitude());
    }

    @ParameterizedTest
    @CsvSource({"90.000001,0", "-90.000001,0", "0,180.000001", "0,-180.000001"})
    void rejeitaCoordenadasForaDosLimites(String latitude, String longitude) {
        assertThrows(IllegalArgumentException.class, () -> criar(latitude, longitude));
    }

    @Test
    void consideraMesmoValorComEscalasDecimaisDiferentes() {
        Localizacao primeira = criar("-7.48", "-36.28");
        Localizacao segunda = criar("-7.480000", "-36.280000");
        var localizacoes = new HashSet<Localizacao>();

        localizacoes.add(primeira);
        localizacoes.add(segunda);

        assertEquals(primeira, segunda);
        assertEquals(primeira.hashCode(), segunda.hashCode());
        assertEquals(1, localizacoes.size());
    }

    @Test
    void distingueDatumsDiferentes() {
        Localizacao primeira = criar("-7.48", "-36.28");
        Localizacao segunda = new Localizacao(primeira.getLatitude(), primeira.getLongitude(), DatumGeodesico.WGS_84);

        assertNotEquals(primeira, segunda);
    }

    @Test
    void exigeCoordenadasEDatum() {
        assertThrows(NullPointerException.class,
                () -> new Localizacao(null, BigDecimal.ZERO, DatumGeodesico.SIRGAS_2000));
        assertThrows(NullPointerException.class,
                () -> new Localizacao(BigDecimal.ZERO, null, DatumGeodesico.SIRGAS_2000));
        assertThrows(NullPointerException.class,
                () -> new Localizacao(BigDecimal.ZERO, BigDecimal.ZERO, null));
    }

    private Localizacao criar(String latitude, String longitude) {
        return new Localizacao(new BigDecimal(latitude), new BigDecimal(longitude), DatumGeodesico.SIRGAS_2000);
    }
}
