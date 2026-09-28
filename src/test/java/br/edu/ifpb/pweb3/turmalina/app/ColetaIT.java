package br.edu.ifpb.pweb3.turmalina.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import br.edu.ifpb.pweb3.turmalina.dominio.Amostra;
import br.edu.ifpb.pweb3.turmalina.dominio.Caverna;
import br.edu.ifpb.pweb3.turmalina.dominio.Coleta;
import br.edu.ifpb.pweb3.turmalina.dominio.Expedicao;
import br.edu.ifpb.pweb3.turmalina.dominio.Pesquisador;
import br.edu.ifpb.pweb3.turmalina.dominio.PlanoSeguranca;
import br.edu.ifpb.pweb3.turmalina.dominio.Setor;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.CategoriaAmostra;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.CondicaoConservacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.DatumGeodesico;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.NivelDificuldade;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.Titulacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeMedida;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Endereco;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Localizacao;

class ColetaIT extends IntegracaoPostgres {

    @Test
    void deveSalvarColetaEComprovarCarregamentoLazyDasAmostras() throws Exception {
        Caverna caverna = new Caverna("Caverna do Diabo", "CD-01", "Afloramento calcário", UnidadeFederativa.PB, 
                new Localizacao(new BigDecimal("-7.12"), new BigDecimal("-34.88"), DatumGeodesico.SIRGAS_2000));
        Setor setor = caverna.adicionarSetor(new Setor("Setor Sul", NivelDificuldade.BAIXO, new BigDecimal("10.0"), new BigDecimal("50.0"), false));

        Endereco endereco = new Endereco("Rua A", "10", null, "Centro", "João Pessoa", UnidadeFederativa.PB, "58000-000");
        Pesquisador pesquisador = new Pesquisador("Dra. Ana", "111.222.333-44", LocalDate.of(1985, 3, 20), "ana@ifpb.edu.br", "(83) 99999-0000", endereco, "MAT123", "Espeleologia", Titulacao.DOUTORADO, new BigDecimal("100.00"));

        PlanoSeguranca plano = new PlanoSeguranca("Plano Base", "Risco", 100, "999", true);
        Expedicao expedicao = new Expedicao("EXP-01", "Expedição Turmalina", "Levantamento", caverna, LocalDateTime.of(2026, 8, 10, 8, 0), LocalDateTime.of(2026, 8, 15, 18, 0), new BigDecimal("5000.00"), 5, plano);
        expedicao.abrangerSetor(setor);

        Coleta coleta = expedicao.registrarColeta(setor, pesquisador, LocalDateTime.of(2026, 8, 11, 10, 0), "Coleta Manual");
        coleta.registrarCondicoes(new BigDecimal("22.5"), new BigDecimal("85.0"), new BigDecimal("10.0"));

        Amostra amostra = new Amostra("AMS-001", CategoriaAmostra.FAUNA, new BigDecimal("500.00"), UnidadeMedida.GRAMA, LocalDate.of(2026, 8, 11), CondicaoConservacao.INTEGRA, false);
        coleta.adicionarAmostra(amostra);

        transacao(em -> {
            em.persist(caverna);
            em.persist(pesquisador);
            em.persist(expedicao);
            return null;
        });

        transacao(em -> {
            Coleta encontrada = em.find(Coleta.class, coleta.getId());
            assertNotNull(encontrada);
            assertEquals("Coleta Manual", encontrada.getMetodo());
            
            // Comprova o carregamento LAZY da coleção de amostras
            assertFalse(fabrica().getPersistenceUnitUtil().isLoaded(encontrada, "amostras"));
            return null;
        });

        assertEquals("numeric", tipoDaColuna("coleta", "umidade_relativa"));
        assertEquals("numeric", tipoDaColuna("amostra", "quantidade"));
    }

    @Test
    void deveRejeitarCodigoDeCampoDeAmostraDuplicado() throws Exception {
        Caverna caverna = new Caverna("Caverna A", "CA-01", "Rocha", UnidadeFederativa.PB, 
                new Localizacao(new BigDecimal("-7.12"), new BigDecimal("-34.88"), DatumGeodesico.SIRGAS_2000));
        Setor setor = caverna.adicionarSetor(new Setor("Setor A", NivelDificuldade.BAIXO, new BigDecimal("10.0"), new BigDecimal("50.0"), false));

        Endereco endereco = new Endereco("Rua B", "20", null, "Centro", "João Pessoa", UnidadeFederativa.PB, "58000-000");
        Pesquisador pesquisador = new Pesquisador("Dr. Bruno", "222.333.444-55", LocalDate.of(1990, 1, 1), "bruno@ifpb.edu.br", "(83) 98888-1111", endereco, "MAT456", "Biologia", Titulacao.MESTRADO, new BigDecimal("100.00"));

        PlanoSeguranca plano = new PlanoSeguranca("Plano", "Risco", 10, "999", false);
        Expedicao expedicao = new Expedicao("EXP-02", "Expedição B", "Pesquisa", caverna, LocalDateTime.of(2026, 8, 10, 8, 0), LocalDateTime.of(2026, 8, 12, 18, 0), new BigDecimal("2000.00"), 3, plano);
        expedicao.abrangerSetor(setor);

        Coleta coleta1 = expedicao.registrarColeta(setor, pesquisador, LocalDateTime.of(2026, 8, 11, 9, 0), "Raspagem");
        Coleta coleta2 = expedicao.registrarColeta(setor, pesquisador, LocalDateTime.of(2026, 8, 11, 14, 0), "Filtração");
        
        Amostra am1 = new Amostra("AMS-DUP", CategoriaAmostra.AGUA, new BigDecimal("100.00"), UnidadeMedida.MILILITRO, LocalDate.of(2026, 8, 11), CondicaoConservacao.INTEGRA, false);
        Amostra am2 = new Amostra("AMS-DUP", CategoriaAmostra.SEDIMENTO, new BigDecimal("200.00"), UnidadeMedida.GRAMA, LocalDate.of(2026, 8, 11), CondicaoConservacao.INTEGRA, false);

        coleta1.adicionarAmostra(am1);

        transacao(em -> {
            em.persist(caverna);
            em.persist(pesquisador);
            em.persist(expedicao);
            return null;
        });

        coleta2.adicionarAmostra(am2);

        Exception ex = assertThrows(Exception.class, () -> transacao(em -> {
            em.merge(expedicao);
            return null;
        }));

        exigirSqlState(ex, "23505");
    }
}