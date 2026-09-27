package br.edu.ifpb.pweb3.turmalina.app;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfiguracaoTest {

    @Test
    void usaPadroesDaUnidadeQuandoAmbienteNaoDefineConexao() {
        assertTrue(Configuracao.propriedadesDoAmbiente(Map.of()).isEmpty());
    }

    @Test
    void ignoraVariaveisVaziasEVariaveisNaoRelacionadas() {
        var propriedades = Configuracao.propriedadesDoAmbiente(Map.of(
                "TURMALINA_DB_URL", " ", "TURMALINA_DB_PASSWORD", "", "OUTRA_VARIAVEL", "valor"));

        assertTrue(propriedades.isEmpty());
    }

    @Test
    void sobrescreveApenasConexaoPreservandoConteudoDaSenha() {
        var propriedades = Configuracao.propriedadesDoAmbiente(Map.of(
                "TURMALINA_DB_URL", "jdbc:postgresql://localhost:5435/turmalina_test",
                "TURMALINA_DB_USER", "usuario_teste",
                "TURMALINA_DB_PASSWORD", " senha teste "));

        assertEquals(Map.of(
                "jakarta.persistence.jdbc.url", "jdbc:postgresql://localhost:5435/turmalina_test",
                "jakarta.persistence.jdbc.user", "usuario_teste",
                "jakarta.persistence.jdbc.password", " senha teste "), propriedades);
    }
}
