package br.edu.ifpb.pweb3.turmalina.app;

import br.edu.ifpb.pweb3.turmalina.consulta.ArquivoConsultas;
import br.edu.ifpb.pweb3.turmalina.consulta.ExpedicaoConsultas;
import br.edu.ifpb.pweb3.turmalina.consulta.dto.ExpedicaoDetalhe;
import br.edu.ifpb.pweb3.turmalina.consulta.dto.ExpedicaoResumo;
import br.edu.ifpb.pweb3.turmalina.consulta.dto.ParticipanteResumo;
import br.edu.ifpb.pweb3.turmalina.dominio.Amostra;
import br.edu.ifpb.pweb3.turmalina.dominio.AutorizacaoAmbiental;
import br.edu.ifpb.pweb3.turmalina.dominio.Caverna;
import br.edu.ifpb.pweb3.turmalina.dominio.Coleta;
import br.edu.ifpb.pweb3.turmalina.dominio.Expedicao;
import br.edu.ifpb.pweb3.turmalina.dominio.GuiaEspeleologia;
import br.edu.ifpb.pweb3.turmalina.dominio.Pesquisador;
import br.edu.ifpb.pweb3.turmalina.dominio.Pessoa;
import br.edu.ifpb.pweb3.turmalina.dominio.PlanoSeguranca;
import br.edu.ifpb.pweb3.turmalina.dominio.RelatorioFinal;
import br.edu.ifpb.pweb3.turmalina.dominio.Setor;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.CategoriaAmostra;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.CondicaoConservacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.DatumGeodesico;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.NivelCertificacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.NivelDificuldade;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.PapelParticipante;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoAutorizacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoExpedicao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.Titulacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeMedida;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Endereco;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Localizacao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpedicaoConsultasIT extends IntegracaoPostgres {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);
    private static final BigDecimal DIARIA = new BigDecimal("120.00");
    private static final byte[] MAPA = {10, 20, 30, 40};
    private static final byte[] PDF_VIGENTE = {37, 80, 68, 70, 1};
    private static final byte[] PDF_VENCIDA = {37, 80, 68, 70, 2};
    private static final byte[] ARQUIVO = {1, 2, 3, 4, 5};
    private static final byte[] FOTO = {9, 8, 7};

    @Test
    void listaExpedicoesDoPeriodoESituacaoComUmaConsulta() {
        List<String> esperadas = transacao(em -> {
            Caverna alfa = novaCaverna(em, "Gruta Alfa");
            Caverna beta = novaCaverna(em, "Furna Beta");
            Expedicao primeira = novaExpedicao(em, alfa, dia(2030, 3, 1), dia(2030, 3, 5));
            Expedicao segunda = novaExpedicao(em, beta, dia(2030, 3, 10), dia(2030, 3, 12));
            novaExpedicao(em, alfa, dia(2030, 3, 3), dia(2030, 3, 4)).cancelar(false);
            novaExpedicao(em, beta, dia(2030, 6, 1), dia(2030, 6, 3));
            return List.of(primeira.getCodigo(), segunda.getCodigo());
        });

        transacao(em -> {
            ExpedicaoConsultas consultas = new ExpedicaoConsultas(em);
            List<ExpedicaoResumo> resumos = contarComandos(1, () -> consultas.listarPorPeriodoESituacao(
                    dia(2030, 3, 1), dia(2030, 3, 31),
                    EnumSet.of(SituacaoExpedicao.PLANEJADA, SituacaoExpedicao.AUTORIZADA)));

            assertEquals(esperadas, resumos.stream().map(ExpedicaoResumo::codigo).toList());
            assertEquals(List.of("Gruta Alfa", "Furna Beta"), resumos.stream().map(ExpedicaoResumo::caverna).toList());
            assertEquals(dia(2030, 3, 1), resumos.getFirst().inicioPrevisto());
            assertEquals(dia(2030, 3, 5), resumos.getFirst().terminoPrevisto());
            assertEquals(SituacaoExpedicao.PLANEJADA, resumos.getFirst().situacao());
            assertEquals(0, em.unwrap(Session.class).getStatistics().getEntityCount());
            return null;
        });
    }

    @Test
    void carregarEntidadesEAcessarACavernaGeraUmaConsultaPorCaverna() {
        transacao(em -> {
            Caverna alfa = novaCaverna(em, "Gruta Alfa");
            Caverna beta = novaCaverna(em, "Furna Beta");
            novaExpedicao(em, alfa, dia(2031, 3, 1), dia(2031, 3, 5));
            novaExpedicao(em, beta, dia(2031, 3, 10), dia(2031, 3, 12));
            novaExpedicao(em, alfa, dia(2031, 3, 15), dia(2031, 3, 16));
            return null;
        });

        transacao(em -> {
            List<String> cavernas = contarComandos(3, () -> em.createQuery(
                            "select e from Expedicao e where e.inicioPrevisto between :de and :ate "
                                    + "order by e.inicioPrevisto", Expedicao.class)
                    .setParameter("de", dia(2031, 3, 1))
                    .setParameter("ate", dia(2031, 3, 31))
                    .getResultList()
                    .stream()
                    .map(e -> e.getCaverna().getNomeOficial())
                    .toList());

            assertEquals(List.of("Gruta Alfa", "Furna Beta", "Gruta Alfa"), cavernas);
            return null;
        });
    }

    @Test
    void carregaDetalhesComParticipantesEmDuasConsultasSemArquivos() {
        Long id = transacao(em -> {
            Caverna caverna = novaCaverna(em, "Gruta Alfa");
            Setor entrada = caverna.adicionarSetor(novoSetor("Entrada"));
            Setor salao = caverna.adicionarSetor(novoSetor("Salão principal"));
            Expedicao expedicao = novaExpedicao(em, caverna, dia(2026, 10, 5), dia(2026, 10, 9));
            expedicao.abrangerSetor(entrada);
            expedicao.abrangerSetor(salao);
            expedicao.getPlanoSeguranca().setMapaRota(MAPA);
            expedicao.registrarAutorizacao(novaAutorizacao(SituacaoAutorizacao.VIGENTE, PDF_VIGENTE));
            expedicao.adicionarParticipante(novoPesquisador(em, "Ana Beatriz Lima"), PapelParticipante.COORDENADOR, DIARIA, 5)
                    .confirmar(HOJE);
            expedicao.adicionarParticipante(novoGuia(em, "Maria Eduarda Souto"), PapelParticipante.GUIA, DIARIA, 5);
            expedicao.adicionarParticipante(novaPessoa(em, "Carlos Mendes"), PapelParticipante.APOIO_TECNICO, DIARIA, 3);
            return expedicao.getId();
        });
        PersistenceUnitUtil util = fabrica().getPersistenceUnitUtil();

        transacao(em -> {
            ExpedicaoConsultas consultas = new ExpedicaoConsultas(em);
            ExpedicaoDetalhe detalhe = contarComandos(2, () -> consultas.carregarDetalhes(id)).orElseThrow();
            Expedicao expedicao = detalhe.expedicao();

            assertTrue(util.isLoaded(expedicao.getCaverna()));
            assertTrue(util.isLoaded(expedicao, "setores"));
            assertFalse(util.isLoaded(expedicao.getPlanoSeguranca()));
            assertFalse(util.isLoaded(expedicao, "autorizacoes"));
            assertFalse(util.isLoaded(expedicao, "participacoes"));
            assertEquals("Gruta Alfa", expedicao.getCaverna().getNomeOficial());
            assertEquals(2, expedicao.getSetores().size());

            List<ParticipanteResumo> participantes = detalhe.participantes();
            assertEquals(List.of(PapelParticipante.APOIO_TECNICO, PapelParticipante.COORDENADOR, PapelParticipante.GUIA),
                    participantes.stream().map(ParticipanteResumo::papel).toList());
            assertEquals(List.of("Carlos Mendes", "Ana Beatriz Lima", "Maria Eduarda Souto"),
                    participantes.stream().map(ParticipanteResumo::nome).toList());
            assertEquals(HOJE, participantes.get(1).dataConfirmacao());
            assertTrue(participantes.stream().noneMatch(ParticipanteResumo::presencaConfirmada));
            return null;
        });
    }

    @Test
    void naoEncontraDetalhesDeExpedicaoInexistente() {
        transacao(em -> {
            assertTrue(new ExpedicaoConsultas(em).carregarDetalhes(-1L).isEmpty());
            return null;
        });
    }

    @Test
    void baixaCadaArquivoSeparadamenteComUmaConsultaSemCarregarEntidades() {
        Long[] ids = transacao(em -> {
            Caverna caverna = novaCaverna(em, "Gruta Alfa");
            Setor setor = caverna.adicionarSetor(novoSetor("Entrada"));
            Expedicao expedicao = novaExpedicao(em, caverna, dia(2026, 10, 5), dia(2026, 10, 9));
            expedicao.abrangerSetor(setor);
            expedicao.getPlanoSeguranca().setMapaRota(MAPA);
            AutorizacaoAmbiental vencida = expedicao.registrarAutorizacao(
                    novaAutorizacao(SituacaoAutorizacao.VENCIDA, PDF_VENCIDA));
            expedicao.registrarAutorizacao(novaAutorizacao(SituacaoAutorizacao.VIGENTE, PDF_VIGENTE));
            Coleta coleta = expedicao.registrarColeta(setor, novoPesquisador(em, "Ana Beatriz Lima"),
                    LocalDateTime.of(2026, 10, 6, 10, 0), "Coleta manual");
            Amostra amostra = coleta.adicionarAmostra(new Amostra("AMS-" + UUID.randomUUID().toString().substring(0, 12),
                    CategoriaAmostra.SEDIMENTO, new BigDecimal("150.00"), UnidadeMedida.GRAMA, LocalDate.of(2026, 10, 6),
                    CondicaoConservacao.INTEGRA, false));
            amostra.setFotografia(FOTO);
            expedicao.autorizar(HOJE);
            expedicao.iniciar();
            expedicao.concluir(new BigDecimal("5000.00"));
            expedicao.anexarRelatorioFinal(new RelatorioFinal("Relatório final", "Resumo", HOJE, 42, ARQUIVO));
            em.flush();
            return new Long[]{expedicao.getId(), vencida.getId(), amostra.getId()};
        });

        transacao(em -> {
            ArquivoConsultas arquivos = new ArquivoConsultas(em);
            assertArrayEquals(MAPA, contarComandos(1, () -> arquivos.mapaDeRota(ids[0])).orElseThrow());
            assertArrayEquals(PDF_VIGENTE, contarComandos(1, () -> arquivos.pdfAutorizacaoVigente(ids[0])).orElseThrow());
            assertArrayEquals(PDF_VENCIDA, contarComandos(1, () -> arquivos.pdfAutorizacao(ids[1])).orElseThrow());
            assertArrayEquals(ARQUIVO, contarComandos(1, () -> arquivos.arquivoRelatorioFinal(ids[0])).orElseThrow());
            assertArrayEquals(FOTO, contarComandos(1, () -> arquivos.fotografiaDaAmostra(ids[2])).orElseThrow());
            assertEquals(0, em.unwrap(Session.class).getStatistics().getEntityCount());
            return null;
        });
    }

    @Test
    void devolveVazioQuandoOArquivoNaoExiste() {
        Long id = transacao(em -> novaExpedicao(em, novaCaverna(em, "Gruta Alfa"),
                dia(2026, 10, 5), dia(2026, 10, 9)).getId());

        transacao(em -> {
            ArquivoConsultas arquivos = new ArquivoConsultas(em);
            assertEquals(Optional.empty(), arquivos.mapaDeRota(id));
            assertEquals(Optional.empty(), arquivos.pdfAutorizacaoVigente(id));
            assertEquals(Optional.empty(), arquivos.arquivoRelatorioFinal(id));
            return null;
        });
    }

    private <T> T contarComandos(long esperado, Supplier<T> consulta) {
        Statistics estatisticas = fabrica().unwrap(SessionFactory.class).getStatistics();
        estatisticas.setStatisticsEnabled(true);
        estatisticas.clear();
        try {
            T resultado = consulta.get();
            assertEquals(esperado, estatisticas.getPrepareStatementCount());
            return resultado;
        } finally {
            estatisticas.setStatisticsEnabled(false);
        }
    }

    private Caverna novaCaverna(EntityManager em, String nome) {
        Caverna caverna = new Caverna(nome, "CAV-" + UUID.randomUUID().toString().substring(0, 20), "Cabaceiras",
                UnidadeFederativa.PB,
                new Localizacao(new BigDecimal("-7.489512"), new BigDecimal("-36.287431"), DatumGeodesico.SIRGAS_2000));
        em.persist(caverna);
        return caverna;
    }

    private Setor novoSetor(String denominacao) {
        return new Setor(denominacao, NivelDificuldade.BAIXO, new BigDecimal("12.50"), BigDecimal.TEN, false);
    }

    private Expedicao novaExpedicao(EntityManager em, Caverna caverna, LocalDateTime inicio, LocalDateTime termino) {
        Expedicao expedicao = new Expedicao("EXP-" + UUID.randomUUID().toString().substring(0, 12),
                "Levantamento da fauna cavernícola", "Registrar a fauna dos salões", caverna, inicio, termino,
                new BigDecimal("12000.00"), 5, new PlanoSeguranca("Retornar pela galeria principal",
                "Portaria do parque", 120, "83999990000", true));
        em.persist(expedicao);
        return expedicao;
    }

    private AutorizacaoAmbiental novaAutorizacao(SituacaoAutorizacao situacao, byte[] pdf) {
        return new AutorizacaoAmbiental("AUT-" + UUID.randomUUID().toString().substring(0, 20), "ICMBio",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 31), situacao, pdf);
    }

    private Pessoa novaPessoa(EntityManager em, String nome) {
        Pessoa pessoa = new Pessoa(nome, cpf(), LocalDate.of(1990, 1, 15), email(), "83999990000", endereco());
        em.persist(pessoa);
        return pessoa;
    }

    private Pesquisador novoPesquisador(EntityManager em, String nome) {
        Pesquisador pesquisador = new Pesquisador(nome, cpf(), LocalDate.of(1985, 3, 14), email(), "83999990001",
                endereco(), "REG-" + UUID.randomUUID().toString().substring(0, 20), "Bioespeleologia",
                Titulacao.DOUTORADO, new BigDecimal("180.00"));
        em.persist(pesquisador);
        return pesquisador;
    }

    private GuiaEspeleologia novoGuia(EntityManager em, String nome) {
        GuiaEspeleologia guia = new GuiaEspeleologia(nome, cpf(), LocalDate.of(1988, 6, 21), email(), "83999990002",
                endereco(), "SBE-" + UUID.randomUUID().toString().substring(0, 8), NivelCertificacao.INTERMEDIARIO,
                LocalDate.of(2027, 6, 30));
        em.persist(guia);
        return guia;
    }

    private Endereco endereco() {
        return new Endereco("Av. Primeiro de Maio", "720", null, "Jaguaribe", "João Pessoa",
                UnidadeFederativa.PB, "58015-435");
    }

    private String cpf() {
        return String.format("%011d", ThreadLocalRandom.current().nextLong(1L, 99_999_999_999L));
    }

    private String email() {
        return "teste-" + UUID.randomUUID() + "@turmalina.org";
    }

    private static LocalDateTime dia(int ano, int mes, int dia) {
        return LocalDateTime.of(ano, mes, dia, 8, 0);
    }
}
