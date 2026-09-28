package br.edu.ifpb.pweb3.turmalina.app;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegracaoPostgres {

    private static final String URL = variavel("TURMALINA_TEST_DB_URL", "jdbc:postgresql://localhost:5435/turmalina_test");
    private static final String USUARIO = variavel("TURMALINA_TEST_DB_USER", "turmalina");
    private static final String SENHA = variavel("TURMALINA_TEST_DB_PASSWORD", "turmalina");

    private final String esquema = "teste_" + UUID.randomUUID().toString().replace("-", "");
    private EntityManagerFactory fabrica;
    private boolean esquemaCriado;

    @BeforeAll
    void iniciarBanco() throws SQLException {
        executarDdl("create schema " + esquema);
        esquemaCriado = true;
        Map<String, Object> propriedades = new HashMap<>(Configuracao.recriacaoDoEsquema(scriptPosCriacao()));
        propriedades.put("jakarta.persistence.jdbc.url", URL + (URL.contains("?") ? "&" : "?") + "currentSchema=" + esquema + ",public");
        propriedades.put("jakarta.persistence.jdbc.user", USUARIO);
        propriedades.put("jakarta.persistence.jdbc.password", SENHA);
        propriedades.put("hibernate.default_schema", esquema);
        fabrica = Persistence.createEntityManagerFactory(Configuracao.UNIDADE_PERSISTENCIA, propriedades);
    }

    protected String scriptPosCriacao() {
        return Configuracao.SCRIPT_POS_CRIACAO;
    }

    @AfterAll
    void encerrarBanco() throws SQLException {
        try {
            if (fabrica != null) {
                fabrica.close();
            }
        } finally {
            if (esquemaCriado) {
                executarDdl("drop schema " + esquema + " cascade");
            }
        }
    }

    protected EntityManagerFactory fabrica() {
        return fabrica;
    }

    protected <T> T transacao(Function<EntityManager, T> trabalho) {
        try (EntityManager em = fabrica.createEntityManager()) {
            em.getTransaction().begin();
            try {
                T resultado = trabalho.apply(em);
                em.getTransaction().commit();
                return resultado;
            } finally {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }
        }
    }

    protected String tipoDaColuna(String tabela, String coluna) throws SQLException {
        String sql = "select data_type from information_schema.columns "
                + "where table_schema = ? and table_name = ? and column_name = ?";
        try (Connection conexao = DriverManager.getConnection(URL, USUARIO, SENHA);
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, esquema);
            comando.setString(2, tabela);
            comando.setString(3, coluna);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? resultado.getString(1) : null;
            }
        }
    }

    protected static void exigirSqlState(Throwable erro, String estado) {
        for (Throwable causa = erro; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && estado.equals(sql.getSQLState())) {
                return;
            }
        }
        throw new AssertionError("SQLSTATE esperado: " + estado, erro);
    }

    private static void executarDdl(String sql) throws SQLException {
        try (Connection conexao = DriverManager.getConnection(URL, USUARIO, SENHA);
             Statement comando = conexao.createStatement()) {
            comando.execute(sql);
        }
    }

    private static String variavel(String nome, String padrao) {
        String valor = System.getenv(nome);
        return valor == null || valor.isBlank() ? padrao : valor;
    }
}
