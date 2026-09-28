package br.edu.ifpb.pweb3.turmalina.app;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import br.edu.ifpb.pweb3.turmalina.consulta.ColetaConsultas;
import br.edu.ifpb.pweb3.turmalina.consulta.EquipamentoConsultas;
import br.edu.ifpb.pweb3.turmalina.consulta.IndicadorConsultas;
import br.edu.ifpb.pweb3.turmalina.consulta.dto.AmostraResumo;
import br.edu.ifpb.pweb3.turmalina.consulta.dto.RankingPesquisador;
import br.edu.ifpb.pweb3.turmalina.dominio.Amostra;
import br.edu.ifpb.pweb3.turmalina.dominio.Caverna;
import br.edu.ifpb.pweb3.turmalina.dominio.Coleta;
import br.edu.ifpb.pweb3.turmalina.dominio.Equipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.Expedicao;
import br.edu.ifpb.pweb3.turmalina.dominio.Pesquisador;
import br.edu.ifpb.pweb3.turmalina.dominio.PlanoSeguranca;
import br.edu.ifpb.pweb3.turmalina.dominio.Setor;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.CategoriaAmostra;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.CondicaoConservacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.DatumGeodesico;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.NivelDificuldade;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.PapelParticipante;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.TipoEquipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.Titulacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeMedida;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Endereco;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Localizacao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;

class ColetaConsultasIT extends IntegracaoPostgres {

    @Test
    void listaColetasComSetorEPesquisadorEmUmaConsulta() {
        Long expedicaoId = transacao(em -> {
            Caverna caverna = novaCaverna(em, "Caverna Alfa");
            Setor setor = caverna.adicionarSetor(novoSetor("Setor Sul"));
            Expedicao expedicao = novaExpedicao(em, caverna);
            expedicao.abrangerSetor(setor);
            
            Pesquisador pesquisador = novoPesquisador(em, "Dr. Roberto");
            expedicao.adicionarParticipante(pesquisador, PapelParticipante.PESQUISADOR, BigDecimal.TEN, 5)
                     .confirmar(LocalDate.now());
            
            expedicao.registrarColeta(setor, pesquisador, LocalDateTime.now(), "Coleta manual");
            expedicao.registrarColeta(setor, pesquisador, LocalDateTime.now().plusHours(1), "Coleta com armadilha");
            em.flush();
            return expedicao.getId();
        });

        PersistenceUnitUtil util = fabrica().getPersistenceUnitUtil();

        transacao(em -> {
            ColetaConsultas consultas = new ColetaConsultas(em);
            List<Coleta> coletas = contarComandos(1, () -> consultas.listarPorExpedicao(expedicaoId));

            assertEquals(2, coletas.size());
            Coleta c1 = coletas.getFirst();
            assertTrue(util.isLoaded(c1.getSetor()), "Setor deveria ter sido carregado via fetch join");
            assertTrue(util.isLoaded(c1.getPesquisadorResponsavel()), "Pesquisador deveria ter sido carregado via fetch join");
            assertFalse(util.isLoaded(c1, "amostras"), "Amostras devem continuar LAZY");
            return null;
        });
    }

    @Test
    void listaAmostrasDaColetaEmUmaConsulta() {
        Long coletaId = transacao(em -> {
            Caverna caverna = novaCaverna(em, "Caverna Beta");
            Setor setor = caverna.adicionarSetor(novoSetor("Setor Norte"));
            Expedicao expedicao = novaExpedicao(em, caverna);
            expedicao.abrangerSetor(setor);
            
            Pesquisador pesquisador = novoPesquisador(em, "Dra. Camila");
            expedicao.adicionarParticipante(pesquisador, PapelParticipante.PESQUISADOR, BigDecimal.TEN, 5)
                     .confirmar(LocalDate.now());
            
            Coleta coleta = expedicao.registrarColeta(setor, pesquisador, LocalDateTime.now(), "Raspagem");
            coleta.adicionarAmostra(novaAmostra("AMS-" + UUID.randomUUID().toString().substring(0, 8)));
            coleta.adicionarAmostra(novaAmostra("AMS-" + UUID.randomUUID().toString().substring(0, 8)));
            em.flush();
            return coleta.getId();
        });

        transacao(em -> {
            ColetaConsultas consultas = new ColetaConsultas(em);
            List<AmostraResumo> amostras = contarComandos(1, () -> consultas.listarAmostras(coletaId));

            assertEquals(2, amostras.size());
            return null;
        });
    }

    @Test
    void listaEquipamentosDisponiveisNoPeriodoEmUmaConsulta() {
        transacao(em -> {
            Equipamento eq = new Equipamento("PAT-" + UUID.randomUUID().toString().substring(0, 8), 
                "Lanterna", TipoEquipamento.ILUMINACAO, "Petzl", new BigDecimal("899.90"), LocalDate.now().minusYears(1), false);
            em.persist(eq);
            return null;
        });

        transacao(em -> {
            EquipamentoConsultas consultas = new EquipamentoConsultas(em);
            Instant inicio = Instant.now().plus(5, ChronoUnit.DAYS);
            Instant fim = inicio.plus(5, ChronoUnit.DAYS);

            List<Equipamento> disponiveis = contarComandos(1, () -> consultas.listarDisponiveis(inicio, fim));

            assertFalse(disponiveis.isEmpty());
            return null;
        });
    }

    @Test
    void listaRankingDePesquisadoresComConsultaNativaSQL() {
        transacao(em -> {
            Caverna caverna = novaCaverna(em, "Caverna Gama");
            Setor setor = caverna.adicionarSetor(novoSetor("Salão Oculto"));
            Expedicao expedicao = novaExpedicao(em, caverna);
            expedicao.abrangerSetor(setor);
            
            Pesquisador pesquisador = novoPesquisador(em, "Dr. Silva");
            expedicao.adicionarParticipante(pesquisador, PapelParticipante.PESQUISADOR, BigDecimal.TEN, 5)
                     .confirmar(LocalDate.now());
            
            Coleta coleta = expedicao.registrarColeta(setor, pesquisador, LocalDateTime.now(), "Observação visual");
            coleta.adicionarAmostra(novaAmostra("AMS-" + UUID.randomUUID().toString().substring(0, 8)));
            em.flush();
            return null;
        });

        transacao(em -> {
            IndicadorConsultas consultas = new IndicadorConsultas(em);
            List<RankingPesquisador> ranking = contarComandos(1, consultas::rankingDePesquisadoresPorAmostras);

            assertFalse(ranking.isEmpty());
            assertEquals(1L, ranking.getFirst().posicao());
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
        Caverna caverna = new Caverna(nome, "CANIE-" + UUID.randomUUID().toString().substring(0, 10), "Cabaceiras",
                UnidadeFederativa.PB,
                new Localizacao(new BigDecimal("-7.489512"), new BigDecimal("-36.287431"), DatumGeodesico.SIRGAS_2000));
        em.persist(caverna);
        return caverna;
    }

    private Setor novoSetor(String denominacao) {
        return new Setor(denominacao, NivelDificuldade.BAIXO, new BigDecimal("12.50"), BigDecimal.TEN, false);
    }

    private Expedicao novaExpedicao(EntityManager em, Caverna caverna) {
        Expedicao expedicao = new Expedicao("EXP-" + UUID.randomUUID().toString().substring(0, 12),
                "Levantamento", "Registrar a fauna", caverna, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(5),
                new BigDecimal("12000.00"), 5, new PlanoSeguranca("Retornar",
                "Portaria do parque", 120, "83999990000", true));
        em.persist(expedicao);
        return expedicao;
    }

    private Pesquisador novoPesquisador(EntityManager em, String nome) {
        Pesquisador pesquisador = new Pesquisador(nome, cpf(), LocalDate.of(1985, 3, 14), email(), "83999990001",
                endereco(), "REG-" + UUID.randomUUID().toString().substring(0, 12), "Bioespeleologia",
                Titulacao.DOUTORADO, new BigDecimal("180.00"));
        em.persist(pesquisador);
        return pesquisador;
    }

    private Amostra novaAmostra(String codigo) {
        return new Amostra(codigo, CategoriaAmostra.SEDIMENTO, new BigDecimal("150.00"), UnidadeMedida.GRAMA, LocalDate.now(),
                CondicaoConservacao.INTEGRA, false);
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
}