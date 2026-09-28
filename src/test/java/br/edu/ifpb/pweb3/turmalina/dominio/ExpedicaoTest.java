package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.DatumGeodesico;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.NivelDificuldade;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.PapelParticipante;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoAutorizacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoExpedicao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.Titulacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Endereco;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Localizacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpedicaoTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 5, 7, 0);
    private static final LocalDateTime TERMINO = LocalDateTime.of(2026, 10, 9, 18, 0);
    private static final LocalDate EMISSAO = LocalDate.of(2026, 9, 1);
    private static final LocalDate VALIDADE = LocalDate.of(2026, 12, 31);
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 1);
    private static final BigDecimal DIARIA = new BigDecimal("120.00");

    @Test
    void comecaPlanejadaComPlanoVinculadoNosDoisLados() {
        PlanoSeguranca plano = criarPlano();
        Expedicao expedicao = criarExpedicao(criarCaverna(), plano, 5);

        assertEquals(SituacaoExpedicao.PLANEJADA, expedicao.getSituacao());
        assertSame(plano, expedicao.getPlanoSeguranca());
        assertSame(expedicao, plano.getExpedicao());
        assertEquals(0, BigDecimal.ZERO.compareTo(expedicao.getCustoRealizado()));
        assertFalse(expedicao.isCancelamentoEmergencial());
    }

    @Test
    void exigePlanoDeSeguranca() {
        assertThrows(NullPointerException.class, () -> criarExpedicao(criarCaverna(), null, 5));
    }

    @Test
    void rejeitaPlanoQueJaPertenceAOutraExpedicao() {
        PlanoSeguranca plano = criarPlano();
        Expedicao primeira = criarExpedicao(criarCaverna(), plano, 5);

        assertThrows(IllegalStateException.class, () -> criarExpedicao(criarCaverna(), plano, 5));

        assertSame(primeira, plano.getExpedicao());
        assertSame(plano, primeira.getPlanoSeguranca());
    }

    @Test
    void rejeitaPeriodoInvalidoESemVagas() {
        Caverna caverna = criarCaverna();

        assertThrows(IllegalArgumentException.class, () -> new Expedicao("EXP-1", "Titulo", "Objetivo", caverna,
                INICIO, INICIO, BigDecimal.TEN, 5, criarPlano()));
        assertThrows(IllegalArgumentException.class, () -> new Expedicao("EXP-1", "Titulo", "Objetivo", caverna,
                INICIO, TERMINO, BigDecimal.TEN, 0, criarPlano()));
    }

    @Test
    void abrangeSomenteSetoresDaCavernaDaExpedicao() {
        Caverna caverna = criarCaverna();
        Setor proprio = caverna.adicionarSetor(criarSetor("Salão principal"));
        Setor deOutra = criarCaverna().adicionarSetor(criarSetor("Galeria"));
        Expedicao expedicao = criarExpedicao(caverna, criarPlano(), 5);

        expedicao.abrangerSetor(proprio);

        assertThrows(IllegalArgumentException.class, () -> expedicao.abrangerSetor(deOutra));
        assertEquals(Set.of(proprio), expedicao.getSetores());
    }

    @Test
    void naoDeixaDeAbrangerSetorComColetaRegistrada() {
        Caverna caverna = criarCaverna();
        Setor comColeta = caverna.adicionarSetor(criarSetor("Salão principal"));
        Setor semColeta = caverna.adicionarSetor(criarSetor("Galeria"));
        Expedicao expedicao = criarExpedicao(caverna, criarPlano(), 5);
        expedicao.abrangerSetor(comColeta);
        expedicao.abrangerSetor(semColeta);
        expedicao.registrarColeta(comColeta, criarPesquisador(), INICIO.plusHours(3), "Coleta manual");

        assertThrows(IllegalStateException.class, () -> expedicao.deixarDeAbrangerSetor(comColeta));
        expedicao.deixarDeAbrangerSetor(semColeta);

        assertEquals(Set.of(comColeta), expedicao.getSetores());
        assertEquals(1, expedicao.getColetas().size());
    }

    @Test
    void limitaQuantidadeDeParticipantes() {
        Expedicao expedicao = criarExpedicao(criarCaverna(), criarPlano(), 1);
        expedicao.adicionarParticipante(criarPessoa("11111111111"), PapelParticipante.COORDENADOR, DIARIA, 3);

        assertThrows(IllegalStateException.class, () -> expedicao.adicionarParticipante(
                criarPessoa("22222222222"), PapelParticipante.GUIA, DIARIA, 3));
        assertEquals(1, expedicao.getParticipacoes().size());
    }

    @Test
    void impedeMesmaPessoaDuasVezesNaExpedicao() {
        Expedicao expedicao = criarExpedicao(criarCaverna(), criarPlano(), 5);
        Pessoa pessoa = criarPessoa("11111111111");
        expedicao.adicionarParticipante(pessoa, PapelParticipante.PESQUISADOR, DIARIA, 3);

        assertThrows(IllegalStateException.class,
                () -> expedicao.adicionarParticipante(pessoa, PapelParticipante.APOIO_TECNICO, DIARIA, 2));
        assertEquals(1, expedicao.getParticipacoes().size());
    }

    @Test
    void registraPresencaSomenteAposConfirmacao() {
        Expedicao expedicao = criarExpedicao(criarCaverna(), criarPlano(), 5);
        Pessoa pessoa = criarPessoa("11111111111");
        Participacao participacao = expedicao.adicionarParticipante(pessoa, PapelParticipante.GUIA,
                new BigDecimal("150.00"), 4);

        assertSame(expedicao, participacao.getExpedicao());
        assertSame(pessoa, participacao.getPessoa());
        assertEquals(0, new BigDecimal("600.00").compareTo(participacao.custoPrevisto()));
        assertThrows(IllegalStateException.class, participacao::registrarPresenca);

        participacao.confirmar(HOJE);
        participacao.registrarPresenca();

        assertEquals(HOJE, participacao.getDataConfirmacao());
        assertTrue(participacao.isPresencaConfirmada());
    }

    @Test
    void percorreSituacoesDoPlanejamentoAteAConclusao() {
        Expedicao expedicao = criarExpedicao(criarCaverna(), criarPlano(), 5);
        expedicao.registrarAutorizacao(criarAutorizacao("AUT-1", SituacaoAutorizacao.VIGENTE));

        expedicao.autorizar(HOJE);
        assertEquals(SituacaoExpedicao.AUTORIZADA, expedicao.getSituacao());
        expedicao.iniciar();
        assertEquals(SituacaoExpedicao.EM_ANDAMENTO, expedicao.getSituacao());
        expedicao.concluir(new BigDecimal("8750.40"));

        assertEquals(SituacaoExpedicao.CONCLUIDA, expedicao.getSituacao());
        assertEquals(0, new BigDecimal("8750.40").compareTo(expedicao.getCustoRealizado()));
    }

    @Test
    void autorizaSomenteComAutorizacaoVigenteDentroDaValidade() {
        Expedicao semAutorizacao = criarExpedicao(criarCaverna(), criarPlano(), 5);
        Expedicao emAnalise = criarExpedicao(criarCaverna(), criarPlano(), 5);
        emAnalise.registrarAutorizacao(criarAutorizacao("AUT-1", SituacaoAutorizacao.EM_ANALISE));
        Expedicao foraDaValidade = criarExpedicao(criarCaverna(), criarPlano(), 5);
        foraDaValidade.registrarAutorizacao(criarAutorizacao("AUT-2", SituacaoAutorizacao.VIGENTE));

        assertThrows(IllegalStateException.class, () -> semAutorizacao.autorizar(HOJE));
        assertThrows(IllegalStateException.class, () -> emAnalise.autorizar(HOJE));
        assertThrows(IllegalStateException.class, () -> foraDaValidade.autorizar(VALIDADE.plusDays(1)));
        assertEquals(SituacaoExpedicao.PLANEJADA, foraDaValidade.getSituacao());
    }

    @Test
    void rejeitaTransicoesForaDeOrdem() {
        Expedicao expedicao = criarExpedicao(criarCaverna(), criarPlano(), 5);

        assertThrows(IllegalStateException.class, expedicao::iniciar);
        assertThrows(IllegalStateException.class, () -> expedicao.concluir(BigDecimal.ONE));
        assertEquals(SituacaoExpedicao.PLANEJADA, expedicao.getSituacao());
    }

    @Test
    void cancelaRegistrandoEmergenciaMasNaoDepoisDeEncerrada() {
        Expedicao cancelada = criarExpedicao(criarCaverna(), criarPlano(), 5);
        cancelada.cancelar(true);
        Expedicao concluida = criarConcluida();

        assertEquals(SituacaoExpedicao.CANCELADA, cancelada.getSituacao());
        assertTrue(cancelada.isCancelamentoEmergencial());
        assertThrows(IllegalStateException.class, () -> cancelada.cancelar(false));
        assertThrows(IllegalStateException.class, () -> concluida.cancelar(true));
        assertEquals(SituacaoExpedicao.CONCLUIDA, concluida.getSituacao());
    }

    @Test
    void aceitaApenasUmaAutorizacaoVigente() {
        Expedicao expedicao = criarExpedicao(criarCaverna(), criarPlano(), 5);
        AutorizacaoAmbiental vigente = expedicao.registrarAutorizacao(
                criarAutorizacao("AUT-1", SituacaoAutorizacao.VIGENTE));
        expedicao.registrarAutorizacao(criarAutorizacao("AUT-2", SituacaoAutorizacao.VENCIDA));

        assertThrows(IllegalStateException.class,
                () -> expedicao.registrarAutorizacao(criarAutorizacao("AUT-3", SituacaoAutorizacao.VIGENTE)));
        assertEquals(2, expedicao.getAutorizacoes().size());
        assertSame(vigente, expedicao.autorizacaoVigente().orElseThrow());
        assertSame(expedicao, vigente.getExpedicao());
    }

    @Test
    void anexaRelatorioFinalSomenteAposConclusao() {
        Expedicao planejada = criarExpedicao(criarCaverna(), criarPlano(), 5);
        Expedicao concluida = criarConcluida();
        RelatorioFinal relatorio = new RelatorioFinal("Relatório final", "Resumo", HOJE, 42, new byte[]{1, 2, 3});

        assertThrows(IllegalStateException.class, () -> planejada.anexarRelatorioFinal(relatorio));
        assertTrue(planejada.getRelatorioFinal().isEmpty());

        concluida.anexarRelatorioFinal(relatorio);

        assertSame(relatorio, concluida.getRelatorioFinal().orElseThrow());
        assertSame(concluida, relatorio.getExpedicao());
    }

    @Test
    void trocaOPlanoPorOutroAindaSemExpedicao() {
        PlanoSeguranca antigo = criarPlano();
        PlanoSeguranca novo = criarPlano();
        Expedicao expedicao = criarExpedicao(criarCaverna(), antigo, 5);

        expedicao.definirPlanoSeguranca(novo);

        assertSame(novo, expedicao.getPlanoSeguranca());
        assertSame(expedicao, novo.getExpedicao());
        assertNull(antigo.getExpedicao());
    }

    @Test
    void trocaDePlanoRejeitadaPreservaOsDoisVinculos() {
        PlanoSeguranca planoDaPrimeira = criarPlano();
        PlanoSeguranca planoDaSegunda = criarPlano();
        Expedicao primeira = criarExpedicao(criarCaverna(), planoDaPrimeira, 5);
        Expedicao segunda = criarExpedicao(criarCaverna(), planoDaSegunda, 5);

        assertThrows(IllegalStateException.class, () -> primeira.definirPlanoSeguranca(planoDaSegunda));

        assertSame(planoDaPrimeira, primeira.getPlanoSeguranca());
        assertSame(primeira, planoDaPrimeira.getExpedicao());
        assertSame(planoDaSegunda, segunda.getPlanoSeguranca());
        assertSame(segunda, planoDaSegunda.getExpedicao());
    }

    @Test
    void trocaDeRelatorioRejeitadaPreservaOsDoisVinculos() {
        Expedicao primeira = criarConcluida();
        Expedicao segunda = criarConcluida();
        RelatorioFinal relatorioDaPrimeira = criarRelatorio();
        RelatorioFinal relatorioDaSegunda = criarRelatorio();
        primeira.anexarRelatorioFinal(relatorioDaPrimeira);
        segunda.anexarRelatorioFinal(relatorioDaSegunda);

        assertThrows(IllegalStateException.class, () -> primeira.anexarRelatorioFinal(relatorioDaSegunda));
        assertThrows(NullPointerException.class, () -> primeira.anexarRelatorioFinal(null));

        assertSame(relatorioDaPrimeira, primeira.getRelatorioFinal().orElseThrow());
        assertSame(primeira, relatorioDaPrimeira.getExpedicao());
        assertSame(segunda, relatorioDaSegunda.getExpedicao());
    }

    @Test
    void impedeAlteracaoDasColecoesForaDoAgregado() {
        Expedicao expedicao = criarExpedicao(criarCaverna(), criarPlano(), 5);

        assertThrows(UnsupportedOperationException.class, () -> expedicao.getParticipacoes().clear());
        assertThrows(UnsupportedOperationException.class, () -> expedicao.getAutorizacoes().clear());
        assertThrows(UnsupportedOperationException.class, () -> expedicao.getSetores().clear());
    }

    private Expedicao criarConcluida() {
        Expedicao expedicao = criarExpedicao(criarCaverna(), criarPlano(), 5);
        expedicao.registrarAutorizacao(criarAutorizacao("AUT-9", SituacaoAutorizacao.VIGENTE));
        expedicao.autorizar(HOJE);
        expedicao.iniciar();
        expedicao.concluir(new BigDecimal("5000.00"));
        return expedicao;
    }

    private Expedicao criarExpedicao(Caverna caverna, PlanoSeguranca plano, int maxParticipantes) {
        return new Expedicao("EXP-2026-01", "Levantamento da fauna cavernícola", "Registrar a fauna dos salões",
                caverna, INICIO, TERMINO, new BigDecimal("12000.00"), maxParticipantes, plano);
    }

    private PlanoSeguranca criarPlano() {
        return new PlanoSeguranca("Retornar pela galeria principal", "Portaria do parque", 120,
                "83999990000", true);
    }

    private RelatorioFinal criarRelatorio() {
        return new RelatorioFinal("Relatório final", "Resumo", HOJE, 42, new byte[]{1, 2, 3});
    }

    private AutorizacaoAmbiental criarAutorizacao(String numero, SituacaoAutorizacao situacao) {
        return new AutorizacaoAmbiental(numero, "ICMBio", EMISSAO, VALIDADE, situacao, new byte[]{1});
    }

    private Caverna criarCaverna() {
        return new Caverna("Gruta de teste", "CAV-001", "Cabaceiras", UnidadeFederativa.PB,
                new Localizacao(new BigDecimal("-7.489512"), new BigDecimal("-36.287431"), DatumGeodesico.SIRGAS_2000));
    }

    private Setor criarSetor(String denominacao) {
        return new Setor(denominacao, NivelDificuldade.BAIXO, new BigDecimal("12.50"), BigDecimal.TEN, false);
    }

    private Pesquisador criarPesquisador() {
        return new Pesquisador("Ana Beatriz Lima", "33333333333", LocalDate.of(1985, 3, 14), "ana@turmalina.org",
                "83999990001", new Endereco("Av. Primeiro de Maio", "720", null, "Jaguaribe", "João Pessoa",
                UnidadeFederativa.PB, "58015-435"), "REG-001", "Bioespeleologia", Titulacao.DOUTORADO,
                new BigDecimal("180.00"));
    }

    private Pessoa criarPessoa(String cpf) {
        return new Pessoa("Carlos Mendes", cpf, LocalDate.of(1990, 1, 15), cpf + "@turmalina.org", "83999990000",
                new Endereco("Av. Primeiro de Maio", "720", null, "Jaguaribe", "João Pessoa",
                        UnidadeFederativa.PB, "58015-435"));
    }
}
