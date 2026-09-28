package br.edu.ifpb.pweb3.turmalina.app;

import br.edu.ifpb.pweb3.turmalina.dominio.Caverna;
import br.edu.ifpb.pweb3.turmalina.dominio.Expedicao;
import br.edu.ifpb.pweb3.turmalina.dominio.PlanoSeguranca;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.DatumGeodesico;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Localizacao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjetosGrandesIT extends IntegracaoPostgres {

    @Test
    void recriarOEsquemaPreservaObjetosGrandesDeForaDaAplicacao() {
        Long externo = transacao(em -> ((Number) em.createNativeQuery(
                "select lo_from_bytea(0, decode('4f757472612061706c696361636169', 'hex'))").getSingleResult()).longValue());
        try {
            recriarEsquema();

            assertTrue(existe(externo));
        } finally {
            transacao(em -> em.createNativeQuery(
                    "select lo_unlink(oid) from pg_largeobject_metadata where cast(oid as bigint) = ?")
                    .setParameter(1, externo)
                    .getResultList());
        }
    }

    @Test
    void apagaOArquivoAntigoAoSubstituirEAoRemoverALinha() {
        Long id = transacao(em -> {
            Expedicao expedicao = novaExpedicao(em);
            expedicao.getPlanoSeguranca().setMapaRota(new byte[]{1, 2, 3});
            return expedicao.getId();
        });
        Long original = oidDoMapa(id);

        transacao(em -> {
            em.find(Expedicao.class, id).getPlanoSeguranca().setMapaRota(new byte[]{4, 5, 6});
            return null;
        });
        Long substituto = oidDoMapa(id);

        assertNotEquals(original, substituto);
        assertFalse(existe(original));
        assertTrue(existe(substituto));

        transacao(em -> {
            em.remove(em.find(Expedicao.class, id));
            return null;
        });

        assertFalse(existe(substituto));
    }

    private void recriarEsquema() {
        Map<String, Object> propriedades = new HashMap<>(Configuracao.recriacaoDoEsquema(Configuracao.SCRIPT_POS_CRIACAO));
        for (String chave : List.of("jakarta.persistence.jdbc.url", "hibernate.default_schema")) {
            propriedades.put(chave, fabrica().getProperties().get(chave));
        }
        propriedades.put("jakarta.persistence.jdbc.user", variavel("TURMALINA_TEST_DB_USER", "turmalina"));
        propriedades.put("jakarta.persistence.jdbc.password", variavel("TURMALINA_TEST_DB_PASSWORD", "turmalina"));
        Persistence.createEntityManagerFactory(Configuracao.UNIDADE_PERSISTENCIA, propriedades).close();
    }

    private static String variavel(String nome, String padrao) {
        String valor = System.getenv(nome);
        return valor == null || valor.isBlank() ? padrao : valor;
    }

    private Long oidDoMapa(Long expedicaoId) {
        return transacao(em -> ((Number) em.createNativeQuery(
                "select p.mapa_rota from {h-schema}plano_seguranca p "
                        + "join {h-schema}expedicao e on e.plano_seguranca_id = p.id where e.id = ?")
                .setParameter(1, expedicaoId)
                .getSingleResult()).longValue());
    }

    private boolean existe(Long oid) {
        return transacao(em -> ((Number) em.createNativeQuery(
                "select count(*) from pg_largeobject_metadata where cast(oid as bigint) = ?")
                .setParameter(1, oid)
                .getSingleResult()).longValue() == 1);
    }

    private Expedicao novaExpedicao(EntityManager em) {
        Caverna caverna = new Caverna("Gruta de teste", "CAV-" + UUID.randomUUID().toString().substring(0, 20),
                "Cabaceiras", UnidadeFederativa.PB,
                new Localizacao(new BigDecimal("-7.489512"), new BigDecimal("-36.287431"), DatumGeodesico.SIRGAS_2000));
        em.persist(caverna);
        Expedicao expedicao = new Expedicao("EXP-" + UUID.randomUUID().toString().substring(0, 12),
                "Levantamento da fauna cavernícola", "Registrar a fauna dos salões", caverna,
                LocalDateTime.of(2026, 10, 5, 7, 0), LocalDateTime.of(2026, 10, 9, 18, 0),
                new BigDecimal("12000.00"), 5, new PlanoSeguranca("Retornar pela galeria principal",
                "Portaria do parque", 120, "83999990000", true));
        em.persist(expedicao);
        return expedicao;
    }
}
