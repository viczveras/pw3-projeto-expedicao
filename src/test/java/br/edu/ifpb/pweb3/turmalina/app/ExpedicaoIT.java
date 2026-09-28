package br.edu.ifpb.pweb3.turmalina.app;

import br.edu.ifpb.pweb3.turmalina.dominio.AutorizacaoAmbiental;
import br.edu.ifpb.pweb3.turmalina.dominio.Caverna;
import br.edu.ifpb.pweb3.turmalina.dominio.Expedicao;
import br.edu.ifpb.pweb3.turmalina.dominio.Participacao;
import br.edu.ifpb.pweb3.turmalina.dominio.Pessoa;
import br.edu.ifpb.pweb3.turmalina.dominio.PlanoSeguranca;
import br.edu.ifpb.pweb3.turmalina.dominio.RelatorioFinal;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.DatumGeodesico;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.PapelParticipante;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoAutorizacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoExpedicao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Endereco;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Localizacao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PersistenceUnitUtil;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpedicaoIT extends IntegracaoPostgres {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 5, 7, 0);
    private static final LocalDateTime TERMINO = LocalDateTime.of(2026, 10, 9, 18, 0);
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);
    private static final BigDecimal DIARIA = new BigDecimal("120.00");
    private static final byte[] MAPA = {10, 20, 30, 40};
    private static final byte[] PDF = {37, 80, 68, 70};
    private static final byte[] ARQUIVO = {1, 2, 3, 4, 5};

    @Test
    void persistePlanoPorCascataComTiposNativosDoPostgres() throws SQLException {
        Long id = transacao(em -> novaExpedicao(em, 5).getId());

        transacao(em -> {
            Expedicao expedicao = em.find(Expedicao.class, id);
            assertEquals(SituacaoExpedicao.PLANEJADA, expedicao.getSituacao());
            assertNotNull(expedicao.getPlanoSeguranca().getId());
            assertSame(expedicao, expedicao.getPlanoSeguranca().getExpedicao());
            assertEquals("PLANEJADA", valorNativo(em, "select situacao from {h-schema}expedicao where id = ?", id));
            return null;
        });
        assertEquals("timestamp without time zone", tipoDaColuna("expedicao", "inicio_previsto"));
        assertEquals("numeric", tipoDaColuna("expedicao", "orcamento_aprovado"));
        assertEquals("boolean", tipoDaColuna("expedicao", "cancelamento_emergencial"));
        assertEquals("date", tipoDaColuna("autorizacao_ambiental", "data_validade"));
        assertEquals("oid", tipoDaColuna("plano_seguranca", "mapa_rota"));
        assertEquals("oid", tipoDaColuna("autorizacao_ambiental", "arquivo_pdf"));
    }

    @Test
    void exigePlanoObrigatorioEExclusivoNoPostgres() {
        Long[] ids = transacao(em -> new Long[]{novaExpedicao(em, 5).getId(), novaExpedicao(em, 5).getId()});

        PersistenceException compartilhado = assertThrows(PersistenceException.class, () -> transacao(em ->
                em.createNativeQuery("update {h-schema}expedicao set plano_seguranca_id = "
                                + "(select plano_seguranca_id from {h-schema}expedicao where id = ?) where id = ?")
                        .setParameter(1, ids[0])
                        .setParameter(2, ids[1])
                        .executeUpdate()));
        PersistenceException semPlano = assertThrows(PersistenceException.class, () -> transacao(em ->
                em.createNativeQuery("update {h-schema}expedicao set plano_seguranca_id = null where id = ?")
                        .setParameter(1, ids[0])
                        .executeUpdate()));

        exigirSqlState(compartilhado, "23505");
        exigirSqlState(semPlano, "23502");
    }

    @Test
    void impedeMesmaPessoaDuasVezesNoPostgres() {
        Long participacao = transacao(em -> {
            Expedicao expedicao = novaExpedicao(em, 5);
            Participacao nova = expedicao.adicionarParticipante(novaPessoa(em), PapelParticipante.GUIA, DIARIA, 3);
            em.flush();
            return nova.getId();
        });

        PersistenceException erro = assertThrows(PersistenceException.class, () -> transacao(em ->
                em.createNativeQuery("insert into {h-schema}participacao (expedicao_id, pessoa_id, papel, "
                                + "valor_diaria, quantidade_dias_previstos, presenca_confirmada) "
                                + "select expedicao_id, pessoa_id, 'APOIO_TECNICO', valor_diaria, "
                                + "quantidade_dias_previstos, false from {h-schema}participacao where id = ?")
                        .setParameter(1, participacao)
                        .executeUpdate()));

        exigirSqlState(erro, "23505");
    }

    @Test
    void adicionaParticipanteComUmaConsultaERespeitaOLimite() {
        Long id = transacao(em -> {
            Expedicao expedicao = novaExpedicao(em, 3);
            expedicao.adicionarParticipante(novaPessoa(em), PapelParticipante.COORDENADOR, DIARIA, 3);
            expedicao.adicionarParticipante(novaPessoa(em), PapelParticipante.PESQUISADOR, DIARIA, 3);
            return expedicao.getId();
        });
        Long terceira = transacao(em -> novaPessoa(em).getId());
        Long quarta = transacao(em -> novaPessoa(em).getId());
        Statistics estatisticas = fabrica().unwrap(SessionFactory.class).getStatistics();

        transacao(em -> {
            Expedicao expedicao = em.find(Expedicao.class, id);
            estatisticas.setStatisticsEnabled(true);
            estatisticas.clear();
            expedicao.adicionarParticipante(em.getReference(Pessoa.class, terceira), PapelParticipante.GUIA, DIARIA, 2);
            long comandos = estatisticas.getPrepareStatementCount();
            estatisticas.setStatisticsEnabled(false);

            assertEquals(1, comandos);
            assertThrows(IllegalStateException.class, () -> expedicao.adicionarParticipante(
                    em.getReference(Pessoa.class, quarta), PapelParticipante.APOIO_TECNICO, DIARIA, 2));
            return null;
        });

        transacao(em -> {
            assertEquals(3L, ((Number) valorNativo(em,
                    "select count(*) from {h-schema}participacao where expedicao_id = ?", id)).longValue());
            return null;
        });
    }

    @Test
    void gravaTransicoesDeSituacaoERejeitaCustoNegativo() {
        Long id = transacao(em -> {
            Expedicao expedicao = novaExpedicao(em, 5);
            expedicao.registrarAutorizacao(novaAutorizacao());
            expedicao.autorizar(HOJE);
            expedicao.iniciar();
            return expedicao.getId();
        });
        transacao(em -> {
            Expedicao expedicao = em.find(Expedicao.class, id);
            assertEquals(SituacaoExpedicao.EM_ANDAMENTO, expedicao.getSituacao());
            expedicao.concluir(new BigDecimal("8750.40"));
            return null;
        });

        transacao(em -> {
            assertEquals("CONCLUIDA", valorNativo(em, "select situacao from {h-schema}expedicao where id = ?", id));
            assertEquals(0, new BigDecimal("8750.40").compareTo(em.find(Expedicao.class, id).getCustoRealizado()));
            return null;
        });
        PersistenceException erro = assertThrows(PersistenceException.class, () -> transacao(em ->
                em.createNativeQuery("update {h-schema}expedicao set custo_realizado = -1 where id = ?")
                        .setParameter(1, id)
                        .executeUpdate()));
        exigirSqlState(erro, "23514");
    }

    @Test
    void naoCarregaArquivosBinariosJuntoComAExpedicao() {
        Long id = transacao(em -> {
            Expedicao expedicao = novaExpedicao(em, 5);
            expedicao.getPlanoSeguranca().setMapaRota(MAPA);
            expedicao.registrarAutorizacao(novaAutorizacao());
            expedicao.autorizar(HOJE);
            expedicao.iniciar();
            expedicao.concluir(new BigDecimal("5000.00"));
            expedicao.anexarRelatorioFinal(new RelatorioFinal("Relatório final", "Resumo", HOJE, 42, ARQUIVO));
            return expedicao.getId();
        });
        PersistenceUnitUtil util = fabrica().getPersistenceUnitUtil();

        transacao(em -> {
            Expedicao expedicao = em.find(Expedicao.class, id);
            assertFalse(util.isLoaded(expedicao.getPlanoSeguranca()));
            assertFalse(util.isLoaded(expedicao.getRelatorioFinal().orElseThrow()));
            assertFalse(util.isLoaded(expedicao, "autorizacoes"));

            PlanoSeguranca plano = expedicao.getPlanoSeguranca();
            assertEquals("Portaria do parque", plano.getPontoEncontroExterno());
            AutorizacaoAmbiental autorizacao = expedicao.getAutorizacoes().getFirst();
            RelatorioFinal relatorio = expedicao.getRelatorioFinal().orElseThrow();
            assertEquals(42, relatorio.getTotalPaginas());
            assertFalse(util.isLoaded(plano, "mapaRota"));
            assertFalse(util.isLoaded(autorizacao, "arquivoPdf"));
            assertFalse(util.isLoaded(relatorio, "arquivo"));

            assertArrayEquals(MAPA, plano.getMapaRota());
            assertArrayEquals(PDF, autorizacao.getArquivoPdf());
            assertArrayEquals(ARQUIVO, relatorio.getArquivo());
            assertTrue(util.isLoaded(plano, "mapaRota"));
            return null;
        });
    }

    private Expedicao novaExpedicao(EntityManager em, int maxParticipantes) {
        Caverna caverna = new Caverna("Gruta de teste", "CAV-" + UUID.randomUUID().toString().substring(0, 20),
                "Cabaceiras", UnidadeFederativa.PB,
                new Localizacao(new BigDecimal("-7.489512"), new BigDecimal("-36.287431"), DatumGeodesico.SIRGAS_2000));
        em.persist(caverna);
        Expedicao expedicao = new Expedicao("EXP-" + UUID.randomUUID().toString().substring(0, 12),
                "Levantamento da fauna cavernícola", "Registrar a fauna dos salões", caverna, INICIO, TERMINO,
                new BigDecimal("12000.00"), maxParticipantes, new PlanoSeguranca("Retornar pela galeria principal",
                "Portaria do parque", 120, "83999990000", true));
        em.persist(expedicao);
        return expedicao;
    }

    private Pessoa novaPessoa(EntityManager em) {
        Pessoa pessoa = new Pessoa("Carlos Mendes", String.format("%011d",
                ThreadLocalRandom.current().nextLong(1L, 99_999_999_999L)), LocalDate.of(1990, 1, 15),
                "teste-" + UUID.randomUUID() + "@turmalina.org", "83999990000",
                new Endereco("Av. Primeiro de Maio", "720", null, "Jaguaribe", "João Pessoa",
                        UnidadeFederativa.PB, "58015-435"));
        em.persist(pessoa);
        return pessoa;
    }

    private AutorizacaoAmbiental novaAutorizacao() {
        return new AutorizacaoAmbiental("AUT-" + UUID.randomUUID().toString().substring(0, 20), "ICMBio",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 31), SituacaoAutorizacao.VIGENTE, PDF);
    }

    private Object valorNativo(EntityManager em, String sql, Long id) {
        return em.createNativeQuery(sql).setParameter(1, id).getSingleResult();
    }
}
