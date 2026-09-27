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

    @Test
    void recriaEsquemaEIncluiScriptQuandoEleExiste() {
        assertEquals(Map.of(
                "hibernate.hbm2ddl.auto", "create",
                "jakarta.persistence.sql-load-script-source", "sql/teste-pos-criacao.sql"),
                Configuracao.recriacaoDoEsquema("sql/teste-pos-criacao.sql"));
    }

    @Test
    void recriaEsquemaSemScriptQuandoEleAindaNaoExiste() {
        assertEquals(Map.of("hibernate.hbm2ddl.auto", "create"),
                Configuracao.recriacaoDoEsquema("META-INF/sql/inexistente.sql"));
    }

    @Test
    void demonstracaoRecriaEsquemaEExibeSqlEEstatisticas() {
        var propriedades = Configuracao.propriedadesDaDemonstracao();

        assertEquals("create", propriedades.get("hibernate.hbm2ddl.auto"));
        assertEquals("true", propriedades.get("hibernate.show_sql"));
        assertEquals("true", propriedades.get("hibernate.format_sql"));
        assertEquals("true", propriedades.get("hibernate.generate_statistics"));
    }
}
