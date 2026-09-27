package br.edu.ifpb.pweb3.turmalina.app;

import br.edu.ifpb.pweb3.turmalina.dominio.Caverna;
import br.edu.ifpb.pweb3.turmalina.dominio.Setor;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.CondicaoSetor;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.DatumGeodesico;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.NivelDificuldade;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Localizacao;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScriptPosCriacaoIT extends IntegracaoPostgres {

    @Override
    protected String scriptPosCriacao() {
        return "sql/teste-pos-criacao.sql";
    }

    @Test
    void executaScriptNoEsquemaDoTesteDepoisDeCriarAsTabelas() {
        Long indices = transacao(em -> ((Number) em.createNativeQuery(
                "select count(*) from pg_indexes where schemaname = current_schema() "
                        + "and indexname = 'uk_teste_setor_interditado_por_caverna'")
                .getSingleResult()).longValue());

        assertEquals(1L, indices);
    }

    @Test
    void aplicaIndiceUnicoParcialCriadoPeloScript() {
        transacao(em -> {
            Caverna caverna = criarCaverna();
            caverna.adicionarSetor(criarSetor("Galeria", CondicaoSetor.INTERDITADO));
            caverna.adicionarSetor(criarSetor("Salao", CondicaoSetor.EM_AVALIACAO));
            em.persist(caverna);
            return null;
        });

        PersistenceException erro = assertThrows(PersistenceException.class, () -> transacao(em -> {
            Caverna caverna = criarCaverna();
            caverna.adicionarSetor(criarSetor("Galeria", CondicaoSetor.INTERDITADO));
            caverna.adicionarSetor(criarSetor("Salao", CondicaoSetor.INTERDITADO));
            em.persist(caverna);
            return null;
        }));

        exigirSqlState(erro, "23505");
    }

    private Caverna criarCaverna() {
        return new Caverna("Gruta de teste", "TESTE-" + UUID.randomUUID().toString().substring(0, 20),
                "Cabaceiras", UnidadeFederativa.PB,
                new Localizacao(new BigDecimal("-7.489512"), new BigDecimal("-36.287431"), DatumGeodesico.SIRGAS_2000));
    }

    private Setor criarSetor(String denominacao, CondicaoSetor condicao) {
        Setor setor = new Setor(denominacao, NivelDificuldade.BAIXO, new BigDecimal("12.50"), BigDecimal.TEN, false);
        setor.setCondicaoCorrente(condicao);
        return setor;
    }
}
