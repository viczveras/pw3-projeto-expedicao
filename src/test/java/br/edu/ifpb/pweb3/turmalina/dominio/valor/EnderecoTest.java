package br.edu.ifpb.pweb3.turmalina.dominio.valor;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnderecoTest {

    @Test
    void normalizaCepFormatadoPreservandoZerosIniciais() {
        Endereco endereco = criar("01001-000", null);

        assertEquals("01001000", endereco.getCep());
        assertNull(endereco.getComplemento());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1234567", "123456789"})
    void rejeitaCepComQuantidadeInvalidaDeDigitos(String cep) {
        assertThrows(IllegalArgumentException.class, () -> criar(cep, null));
    }

    @Test
    void consideraIgualEnderecoComCepNormalizado() {
        Endereco formatado = criar("58015-435", "Sala 2");
        Endereco normalizado = criar("58015435", "Sala 2");

        assertEquals(formatado, normalizado);
        assertEquals(formatado.hashCode(), normalizado.hashCode());
    }

    @Test
    void distingueComplementosDiferentes() {
        assertNotEquals(criar("58015435", "Sala 1"), criar("58015435", "Sala 2"));
    }

    @Test
    void exigeCep() {
        assertThrows(NullPointerException.class, () -> criar(null, null));
    }

    private Endereco criar(String cep, String complemento) {
        return new Endereco("Rua do Campus", "10", complemento, "Centro",
                "João Pessoa", UnidadeFederativa.PB, cep);
    }
}
