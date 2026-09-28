package br.edu.ifpb.pweb3.turmalina.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.DatumGeodesico;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.NivelDificuldade;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoValidacaoColeta;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.Titulacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Endereco;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Localizacao;

class ColetaTest {

    private Expedicao criarExpedicaoValida() {
        Caverna caverna = new Caverna("Caverna do Diabo", "CD-01", "Afloramento calcário", UnidadeFederativa.PB, 
                new Localizacao(new BigDecimal("-7.12"), new BigDecimal("-34.88"), DatumGeodesico.SIRGAS_2000));
        
        PlanoSeguranca plano = new PlanoSeguranca("Plano Base", "Risco", 100, "999", true);
        return new Expedicao("EXP-01", "Expedição Turmalina", "Levantamento", caverna, LocalDateTime.now(), LocalDateTime.now().plusDays(5), new BigDecimal("5000.00"), 5, plano);
    }

    private Setor criarSetorValido(Expedicao expedicao) {
        Setor setor = new Setor("Setor Sul", NivelDificuldade.BAIXO, new BigDecimal("10.0"), new BigDecimal("50.0"), false);
        expedicao.getCaverna().adicionarSetor(setor);
        expedicao.abrangerSetor(setor);
        return setor;
    }

    private Pesquisador criarPesquisadorValido() {
        Endereco endereco = new Endereco("Rua A", "10", null, "Centro", "João Pessoa", UnidadeFederativa.PB, "58000-000");
        return new Pesquisador("Dra. Ana", "111.222.333-44", LocalDate.of(1985, 3, 20), "ana@ifpb.edu.br", "(83) 99999-0000", endereco, "MAT123", "Espeleologia", Titulacao.DOUTORADO, new BigDecimal("100.00"));
    }

    @Test
    void deveRegistrarCondicoesEValidarColeta() {
        Expedicao expedicao = criarExpedicaoValida();
        Setor setor = criarSetorValido(expedicao);
        Pesquisador pesquisador = criarPesquisadorValido();

        Coleta coleta = new Coleta(expedicao, setor, pesquisador, LocalDateTime.now(), "Coleta Manual");

        assertEquals(SituacaoValidacaoColeta.PENDENTE, coleta.getSituacaoValidacao());

        coleta.registrarCondicoes(new BigDecimal("22.5"), new BigDecimal("85.0"), new BigDecimal("10.0"));
        assertEquals(new BigDecimal("85.0"), coleta.getUmidadeRelativa());
        assertEquals(new BigDecimal("22.5"), coleta.getTemperatura());
        assertEquals(new BigDecimal("10.0"), coleta.getProfundidade());

        coleta.validar();
        assertEquals(SituacaoValidacaoColeta.VALIDADA, coleta.getSituacaoValidacao());
    }

    @Test
    void deveRejeitarColetaComMotivo() {
        Expedicao expedicao = criarExpedicaoValida();
        Setor setor = criarSetorValido(expedicao);
        Pesquisador pesquisador = criarPesquisadorValido();

        Coleta coleta = new Coleta(expedicao, setor, pesquisador, LocalDateTime.now(), "Raspagem");

        coleta.rejeitar("Amostra contaminada");

        assertEquals(SituacaoValidacaoColeta.REJEITADA, coleta.getSituacaoValidacao());
        assertEquals("Amostra contaminada", coleta.getObservacoes());
    }
}