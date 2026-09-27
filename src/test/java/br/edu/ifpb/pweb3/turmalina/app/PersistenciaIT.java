package br.edu.ifpb.pweb3.turmalina.app;

import br.edu.ifpb.pweb3.turmalina.dominio.Caverna;
import br.edu.ifpb.pweb3.turmalina.dominio.Setor;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.DatumGeodesico;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.NivelDificuldade;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Localizacao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenciaIT {

    private static final String ESQUEMA = "teste_" + UUID.randomUUID().toString().replace("-", "");
    private static final String URL = variavel("TURMALINA_TEST_DB_URL", "jdbc:postgresql://localhost:5435/turmalina_test");
    private static final String USUARIO = variavel("TURMALINA_TEST_DB_USER", "turmalina");
    private static final String SENHA = variavel("TURMALINA_TEST_DB_PASSWORD", "turmalina");
    private static EntityManagerFactory fabrica;
    private static boolean esquemaCriado;

    @BeforeAll
    static void iniciar() throws SQLException {
        executarDdl("create schema " + ESQUEMA);
        esquemaCriado = true;
        Map<String, Object> propriedades = new HashMap<>();
        propriedades.put("jakarta.persistence.jdbc.url", URL);
        propriedades.put("jakarta.persistence.jdbc.user", USUARIO);
        propriedades.put("jakarta.persistence.jdbc.password", SENHA);
        propriedades.put("hibernate.default_schema", ESQUEMA);
        propriedades.put("hibernate.hbm2ddl.auto", "create-drop");
        fabrica = Persistence.createEntityManagerFactory(Configuracao.UNIDADE_PERSISTENCIA, propriedades);
    }

    @AfterAll
    static void encerrar() throws SQLException {
        try {
            if (fabrica != null) {
                fabrica.close();
            }
        } finally {
            if (esquemaCriado) {
                executarDdl("drop schema " + ESQUEMA + " cascade");
            }
        }
    }

    @Test
    void persisteIdentidadeValoresIncorporadosESetoresPorCascata() {
        LocalDate inspecao = LocalDate.of(2026, 9, 27);
        Long id = transacao(em -> {
            Caverna caverna = criarCaverna(codigo());
            caverna.registrarInspecao(inspecao, false);
            caverna.adicionarSetor(criarSetor("Entrada"));
            em.persist(caverna);
            return caverna.getId();
        });

        assertNotNull(id);
        assertTrue(id > 0);
        transacao(em -> {
            Caverna caverna = em.find(Caverna.class, id);
            assertEquals(inspecao, caverna.getDataUltimaInspecao());
            assertFalse(caverna.isAcessoPermitido());
            assertEquals(0, new BigDecimal("-7.489512").compareTo(caverna.getLocalizacao().getLatitude()));
            assertEquals(DatumGeodesico.SIRGAS_2000, caverna.getLocalizacao().getDatum());
            assertEquals(1, caverna.getSetores().size());
            Setor setor = caverna.getSetores().getFirst();
            assertNotNull(setor.getId());
            assertEquals(id, setor.getCaverna().getId());
            assertEquals(NivelDificuldade.BAIXO, setor.getNivelDificuldade());
            return null;
        });
    }

    @Test
    void carregaSetoresSomenteQuandoSolicitados() {
        Long id = cadastrarComSetor();

        transacao(em -> {
            Caverna caverna = em.find(Caverna.class, id);
            assertFalse(fabrica.getPersistenceUnitUtil().isLoaded(caverna, "setores"));
            assertEquals(1, caverna.getSetores().size());
            assertTrue(fabrica.getPersistenceUnitUtil().isLoaded(caverna, "setores"));
            return null;
        });
    }

    @Test
    void impedeCodigoAmbientalDuplicadoNoPostgres() {
        String codigo = codigo();
        transacao(em -> {
            em.persist(criarCaverna(codigo));
            return null;
        });

        PersistenceException erro = assertThrows(PersistenceException.class, () -> transacao(em -> {
            em.persist(criarCaverna(codigo));
            return null;
        }));

        exigirSqlState(erro, "23505");
    }

    @Test
    void impedeProfundidadeNegativaNoPostgres() {
        PersistenceException erro = assertThrows(PersistenceException.class, () -> transacao(em -> {
            Caverna caverna = criarCaverna(codigo());
            caverna.adicionarSetor(new Setor("Invalido", NivelDificuldade.BAIXO,
                    new BigDecimal("-1"), BigDecimal.TEN, false));
            em.persist(caverna);
            return null;
        }));

        exigirSqlState(erro, "23514");
    }

    @Test
    void removeSetorOrfaoSemApagarCaverna() {
        Long cavernaId = cadastrarComSetor();
        Long setorId = transacao(em -> {
            Caverna caverna = em.find(Caverna.class, cavernaId);
            Setor setor = caverna.getSetores().getFirst();
            Long id = setor.getId();
            caverna.removerSetor(setor);
            return id;
        });

        transacao(em -> {
            assertNull(em.find(Setor.class, setorId));
            assertNotNull(em.find(Caverna.class, cavernaId));
            return null;
        });
    }

    private Long cadastrarComSetor() {
        return transacao(em -> {
            Caverna caverna = criarCaverna(codigo());
            caverna.adicionarSetor(criarSetor("Entrada"));
            em.persist(caverna);
            return caverna.getId();
        });
    }

    private Caverna criarCaverna(String codigo) {
        return new Caverna("Gruta de teste", codigo, "Cabaceiras", UnidadeFederativa.PB,
                new Localizacao(new BigDecimal("-7.489512"), new BigDecimal("-36.287431"), DatumGeodesico.SIRGAS_2000));
    }

    private Setor criarSetor(String denominacao) {
        return new Setor(denominacao, NivelDificuldade.BAIXO, new BigDecimal("12.50"), BigDecimal.TEN, false);
    }

    private String codigo() {
        return "TESTE-" + UUID.randomUUID().toString().substring(0, 20);
    }

    private <T> T transacao(Function<EntityManager, T> trabalho) {
        try (EntityManager em = fabrica.createEntityManager()) {
            em.getTransaction().begin();
            try {
                T resultado = trabalho.apply(em);
                em.getTransaction().commit();
                return resultado;
            } catch (RuntimeException erro) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw erro;
            }
        }
    }

    private void exigirSqlState(Throwable erro, String estado) {
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
