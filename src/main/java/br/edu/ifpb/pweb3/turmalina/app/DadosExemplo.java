package br.edu.ifpb.pweb3.turmalina.app;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import br.edu.ifpb.pweb3.turmalina.dominio.Amostra;
import br.edu.ifpb.pweb3.turmalina.dominio.AutorizacaoAmbiental;
import br.edu.ifpb.pweb3.turmalina.dominio.Caverna;
import br.edu.ifpb.pweb3.turmalina.dominio.Coleta;
import br.edu.ifpb.pweb3.turmalina.dominio.Equipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.Expedicao;
import br.edu.ifpb.pweb3.turmalina.dominio.GuiaEspeleologia;
import br.edu.ifpb.pweb3.turmalina.dominio.MovimentacaoEquipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.Pesquisador;
import br.edu.ifpb.pweb3.turmalina.dominio.Pessoa;
import br.edu.ifpb.pweb3.turmalina.dominio.PlanoSeguranca;
import br.edu.ifpb.pweb3.turmalina.dominio.RelatorioFinal;
import br.edu.ifpb.pweb3.turmalina.dominio.Setor;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.CategoriaAmostra;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.CondicaoConservacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.DatumGeodesico;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.EstadoEquipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.NivelCertificacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.NivelDificuldade;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.PapelParticipante;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoAutorizacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.TipoEquipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.Titulacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeMedida;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Endereco;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Localizacao;
import jakarta.persistence.EntityManager;

final class DadosExemplo {

    record Ids(Long expedicaoConcluida, Long coletaComAmostras, Long equipamentoMovimentado) {
    }

    private DadosExemplo() {
    }

    static Ids popular(EntityManager em) {
        LocalDate hoje = LocalDate.now();
        Instant agora = Instant.now();

        Caverna gruta = new Caverna("Gruta da Serra Verde", "CANIE-PB-000123", "Cabaceiras", UnidadeFederativa.PB,
                new Localizacao(new BigDecimal("-7.489512"), new BigDecimal("-36.287431"), DatumGeodesico.SIRGAS_2000));
        gruta.setAltitude(new BigDecimal("512.40"));
        gruta.setExtensaoConhecida(new BigDecimal("1840.00"));
        gruta.registrarInspecao(hoje.minusMonths(2), true);
        Setor salao = gruta.adicionarSetor(new Setor("Salão Principal", NivelDificuldade.BAIXO,
                new BigDecimal("12.50"), new BigDecimal("150.00"), false));
        Setor galeria = gruta.adicionarSetor(new Setor("Galeria das Águas", NivelDificuldade.ALTO,
                new BigDecimal("48.20"), new BigDecimal("620.00"), true));
        gruta.adicionarSetor(new Setor("Fenda Norte", NivelDificuldade.EXTREMO,
                new BigDecimal("75.00"), new BigDecimal("210.00"), false));

        Caverna furna = new Caverna("Furna do Lajedo", "CANIE-PB-000456", "Boqueirão", UnidadeFederativa.PB,
                new Localizacao(new BigDecimal("-7.482003"), new BigDecimal("-36.135877"), DatumGeodesico.SIRGAS_2000));
        furna.adicionarSetor(new Setor("Entrada", NivelDificuldade.MODERADO,
                new BigDecimal("8.00"), new BigDecimal("60.00"), false));
        em.persist(gruta);
        em.persist(furna);

        Endereco enderecoJp = new Endereco("Av. Primeiro de Maio", "720", null, "Jaguaribe",
                "João Pessoa", UnidadeFederativa.PB, "58015-435");
        Pesquisador ana = new Pesquisador("Ana Beatriz Lima", "111.444.777-35", LocalDate.of(1985, 3, 14),
                "ana.lima@exemplo.org", "83999990001", enderecoJp, "IFPB-00421", "Bioespeleologia",
                Titulacao.DOUTORADO, new BigDecimal("180.00"));
        Pesquisador caio = new Pesquisador("Caio Nóbrega", "529.982.247-25", LocalDate.of(1992, 11, 2),
                "caio.nobrega@exemplo.org", "83999990002", enderecoJp, "IFPB-00873", "Geologia",
                Titulacao.MESTRADO, new BigDecimal("150.00"));
        GuiaEspeleologia duda = new GuiaEspeleologia("Maria Eduarda Souto", "390.533.447-05", LocalDate.of(1988, 6, 21),
                "duda.souto@exemplo.org", "83999990003", enderecoJp, "SBE-G-1177",
                NivelCertificacao.AVANCADO, hoje.plusYears(1));
        Pessoa ravi = new Pessoa("Ravi Medeiros", "987.654.321-00", LocalDate.of(1995, 1, 30),
                "ravi.medeiros@exemplo.org", "83999990004", enderecoJp);
        em.persist(ana);
        em.persist(caio);
        em.persist(duda);
        em.persist(ravi);

        Equipamento lanterna = new Equipamento("PAT-000101", "Lanterna de cabeça 1200 lm", TipoEquipamento.ILUMINACAO,
                "Petzl", new BigDecimal("899.90"), hoje.minusYears(1), false);
        Equipamento sonda = new Equipamento("PAT-000102", "Sonda multiparâmetro", TipoEquipamento.MEDICAO_AMBIENTAL,
                "YSI", new BigDecimal("24500.00"), hoje.minusYears(2), true);
        Equipamento radio = new Equipamento("PAT-000103", "Rádio subterrâneo HeyPhone", TipoEquipamento.COMUNICACAO,
                "Artesanal", new BigDecimal("3200.00"), hoje.minusYears(3), false);
        em.persist(lanterna);
        em.persist(sonda);
        em.persist(radio);

        PlanoSeguranca plano1 = new PlanoSeguranca("Retornar pela linha-guia até o Salão Principal...",
                "Estacionamento da trilha de acesso", 180, "83999998888", true);
        plano1.setMapaRota(bytes("MAPA-ROTA-EXP-001"));
        Expedicao exp1 = new Expedicao("EXP-2026-001", "Levantamento bioespeleológico da Galeria das Águas",
                "Inventariar a fauna cavernícola e coletar sedimentos.", gruta,
                LocalDateTime.of(2026, 8, 10, 7, 0), LocalDateTime.of(2026, 8, 15, 18, 0),
                new BigDecimal("35000.00"), 6, plano1);
        exp1.abrangerSetor(salao);
        exp1.abrangerSetor(galeria);
        exp1.registrarAutorizacao(new AutorizacaoAmbiental("ICMBio-2026/0098", "ICMBio",
                hoje.minusYears(1), hoje.minusMonths(6), SituacaoAutorizacao.VENCIDA, bytes("%PDF-antiga")));
        exp1.registrarAutorizacao(new AutorizacaoAmbiental("SUDEMA-2026/0412", "SUDEMA",
                hoje.minusDays(10), hoje.plusMonths(6), SituacaoAutorizacao.VIGENTE, bytes("%PDF-1.7 autorizacao")));
        exp1.adicionarParticipante(ana, PapelParticipante.COORDENADOR, new BigDecimal("250.00"), 6).confirmar(hoje);
        exp1.adicionarParticipante(caio, PapelParticipante.PESQUISADOR, new BigDecimal("200.00"), 6).confirmar(hoje);
        exp1.adicionarParticipante(duda, PapelParticipante.GUIA, new BigDecimal("300.00"), 6).confirmar(hoje);
        exp1.adicionarParticipante(ravi, PapelParticipante.APOIO_TECNICO, new BigDecimal("150.00"), 6);

        Coleta c1 = exp1.registrarColeta(galeria, ana, LocalDateTime.of(2026, 8, 11, 10, 30), "Busca ativa com pinça");
        c1.registrarCondicoes(new BigDecimal("22.40"), new BigDecimal("96.50"), new BigDecimal("31.75"));
        Amostra a1 = c1.adicionarAmostra(new Amostra("GA-0001", CategoriaAmostra.FAUNA, new BigDecimal("0.004250"),
                UnidadeMedida.GRAMA, LocalDate.of(2026, 8, 11), CondicaoConservacao.INTEGRA, false));
        a1.setFotografia(bytes("JPEG-GA-0001"));
        c1.adicionarAmostra(new Amostra("GA-0002", CategoriaAmostra.AGUA, new BigDecimal("250.000000"),
                UnidadeMedida.MILILITRO, LocalDate.of(2026, 8, 11), CondicaoConservacao.INTEGRA, false));
        Coleta c2 = exp1.registrarColeta(salao, caio, LocalDateTime.of(2026, 8, 12, 14, 0), "Testemunho de sedimento");
        c2.adicionarAmostra(new Amostra("SP-0001", CategoriaAmostra.SEDIMENTO, new BigDecimal("1.250000"),
                UnidadeMedida.QUILOGRAMA, LocalDate.of(2026, 8, 12), CondicaoConservacao.INTEGRA, true))
                .setObservacoes("Suspeita de metais pesados: manusear com EPI");

        exp1.autorizar(hoje);
        exp1.iniciar();
        exp1.concluir(new BigDecimal("31280.55"));
        exp1.anexarRelatorioFinal(new RelatorioFinal("Fauna cavernícola da Galeria das Águas",
                "Foram registrados 14 táxons...", hoje, 86, bytes("%PDF-1.7 relatorio final")));
        em.persist(exp1);
        duda.registrarExpedicaoConcluida();

        Expedicao exp2 = new Expedicao("EXP-2026-002", "Mapeamento topográfico da Furna do Lajedo",
                "Atualizar a planta baixa da cavidade.", furna,
                LocalDateTime.of(2026, 10, 5, 7, 0), LocalDateTime.of(2026, 10, 9, 17, 0),
                new BigDecimal("12000.00"), 4,
                new PlanoSeguranca("Evacuar pela entrada única.", "Sede da fazenda", 120, "83999997777", false));
        Expedicao exp3 = new Expedicao("EXP-2026-003", "Monitoramento hídrico da Galeria das Águas",
                "Medir variação de nível no período chuvoso.", gruta,
                LocalDateTime.of(2026, 11, 3, 6, 0), LocalDateTime.of(2026, 11, 6, 18, 0),
                new BigDecimal("18000.00"), 5,
                new PlanoSeguranca("Abortar se o nível subir 20 cm.", "Estacionamento da trilha", 90, "83999996666", true));
        em.persist(exp2);
        em.persist(exp3);

        MovimentacaoEquipamento m1 = new MovimentacaoEquipamento(exp1, lanterna, duda,
                agora.minus(Duration.ofDays(40)), agora.minus(Duration.ofDays(34)), EstadoEquipamento.BOM);
        m1.registrarDevolucao(agora.minus(Duration.ofDays(35)), EstadoEquipamento.BOM, null);

        MovimentacaoEquipamento m2 = new MovimentacaoEquipamento(exp1, sonda, ana,
                agora.minus(Duration.ofDays(5)), agora.minus(Duration.ofDays(1)), EstadoEquipamento.BOM);

        MovimentacaoEquipamento m3 = new MovimentacaoEquipamento(exp2, radio, caio,
                agora.plus(Duration.ofDays(10)), agora.plus(Duration.ofDays(15)), EstadoEquipamento.REGULAR);
        em.persist(m1);
        em.persist(m2);
        em.persist(m3);

        em.flush();
        return new Ids(exp1.getId(), c1.getId(), lanterna.getId());
    }

    private static byte[] bytes(String conteudo) {
        return conteudo.getBytes(StandardCharsets.UTF_8);
    }
}