package br.edu.ifpb.pweb3.turmalina.app;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import br.edu.ifpb.pweb3.turmalina.consulta.ArquivoConsultas;
import br.edu.ifpb.pweb3.turmalina.consulta.ColetaConsultas;
import br.edu.ifpb.pweb3.turmalina.consulta.EquipamentoConsultas;
import br.edu.ifpb.pweb3.turmalina.consulta.ExpedicaoConsultas;
import br.edu.ifpb.pweb3.turmalina.consulta.IndicadorConsultas;
import br.edu.ifpb.pweb3.turmalina.consulta.dto.ExpedicaoDetalhe;
import br.edu.ifpb.pweb3.turmalina.dominio.Coleta;
import br.edu.ifpb.pweb3.turmalina.dominio.Equipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.Expedicao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoExpedicao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public final class Demonstracao {

    private final EntityManagerFactory emf;
    private final Statistics estatisticas;

    private Demonstracao(EntityManagerFactory emf) {
        this.emf = emf;
        this.estatisticas = emf.unwrap(SessionFactory.class).getStatistics();
    }

    public static void main(String[] args) {
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory(
                Configuracao.UNIDADE_PERSISTENCIA, Configuracao.propriedadesDaDemonstracao())) {
            new Demonstracao(emf).executar();
        }
    }

    private void executar() {
        DadosExemplo.Ids ids = emTransacao("Carga de dados de exemplo", DadosExemplo::popular);

        emTransacao("1. Listar expedições por período e situação (projeção)", em -> {
            var lista = new ExpedicaoConsultas(em).listarPorPeriodoESituacao(
                    LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 12, 31, 23, 59),
                    EnumSet.allOf(SituacaoExpedicao.class));
            lista.forEach(r -> System.out.printf("   %s | %s | %s | %s -> %s | %s%n", r.codigo(), r.titulo(),
                    r.caverna(), r.inicioPrevisto(), r.terminoPrevisto(), r.situacao()));
            return null;
        });

        emTransacao("1b. ANTI-EXEMPLO: entidades + acesso à caverna (N+1)", em -> {
            List<Expedicao> lista = em.createQuery("select e from Expedicao e order by e.codigo", Expedicao.class)
                    .getResultList();
            lista.forEach(e -> System.out.printf("   %s | %s%n", e.getCodigo(), e.getCaverna().getNomeOficial()));
            return null;
        });

        emTransacao("2. Detalhes de uma expedição com participantes (sem binários)", em -> {
            ExpedicaoDetalhe d = new ExpedicaoConsultas(em).carregarDetalhes(ids.expedicaoConcluida()).orElseThrow();
            Expedicao e = d.expedicao();
            System.out.printf("   %s - %s (%s), setores: %d%n", e.getCodigo(), e.getTitulo(),
                    e.getCaverna().getNomeOficial(), e.getSetores().size());
            d.participantes().forEach(p -> System.out.printf("   - %s: %s%n", p.papel(), p.nome()));
            return null;
        });

        emTransacao("3. Coletas de uma expedição com setor e pesquisador (fetch join)", em -> {
            List<Coleta> coletas = new ColetaConsultas(em).listarPorExpedicao(ids.expedicaoConcluida());
            coletas.forEach(c -> System.out.printf("   %s | %s | %s | %s%n", c.getDataHora(),
                    c.getSetor().getDenominacao(), c.getPesquisadorResponsavel().getNome(), c.getMetodo()));
            return null;
        });

        emTransacao("4. Amostras de uma coleta, ao abrir os detalhes (sem fotografia)", em -> {
            new ColetaConsultas(em).listarAmostras(ids.coletaComAmostras())
                    .forEach(a -> System.out.printf("   %s | %s | %s %s%n", a.codigoCampo(), a.categoria(),
                            a.quantidade().toPlainString(), a.unidadeMedida().getSimbolo()));
            return null;
        });

        emTransacao("5. Equipamentos disponíveis nos próximos 7 dias (NOT EXISTS, sem histórico)", em -> {
            Instant inicio = Instant.now();
            List<Equipamento> disponiveis = new EquipamentoConsultas(em)
                    .listarDisponiveis(inicio, inicio.plus(Duration.ofDays(7)));
            disponiveis.forEach(eq -> System.out.printf("   %s | %s%n", eq.getCodigoPatrimonial(), eq.getNome()));
            return null;
        });

        emTransacao("6. Download isolado dos arquivos binários", em -> {
            ArquivoConsultas arquivos = new ArquivoConsultas(em);
            Long id = ids.expedicaoConcluida();
            System.out.println("   mapa de rota:     " + tamanho(arquivos.mapaDeRota(id).orElse(null)));
            System.out.println("   autorização PDF:  " + tamanho(arquivos.pdfAutorizacaoVigente(id).orElse(null)));
            System.out.println("   relatório final:  " + tamanho(arquivos.arquivoRelatorioFinal(id).orElse(null)));
            return null;
        });

        emTransacao("7. Consultas nativas externalizadas (recursos do PostgreSQL)", em -> {
            IndicadorConsultas indicadores = new IndicadorConsultas(em);
            System.out.println("   Ranking de pesquisadores (dense_rank):");
            indicadores.rankingDePesquisadoresPorAmostras().forEach(r -> System.out.printf(
                    "   %d. %s | coletas: %d | amostras: %d%n", r.posicao(), r.nome(), r.coletas(), r.amostras()));
            System.out.println("   Resumo financeiro por caverna (COUNT ... FILTER):");
            indicadores.resumoFinanceiroPorCaverna().forEach(r -> System.out.printf(
                    "   %s | expedições: %d (concluídas: %d) | orçamento: %s | custo: %s | executado: %s%%%n",
                    r.caverna(), r.expedicoes(), r.concluidas(), r.orcamentoTotal().toPlainString(),
                    r.custoTotal().toPlainString(),
                    r.percentualExecutado() == null ? "-" : r.percentualExecutado().toPlainString()));
            return null;
        });
    }

    private static String tamanho(byte[] dados) {
        return dados == null ? "(ausente)" : dados.length + " bytes";
    }

    private <T> T emTransacao(String titulo, Function<EntityManager, T> trabalho) {
        System.out.println();
        System.out.println("=".repeat(100));
        System.out.println(titulo);
        System.out.println("=".repeat(100));
        estatisticas.clear();
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                T resultado = trabalho.apply(em);
                tx.commit();
                System.out.printf(">>> comandos SQL emitidos: %d%n", estatisticas.getPrepareStatementCount());
                return resultado;
            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }
}