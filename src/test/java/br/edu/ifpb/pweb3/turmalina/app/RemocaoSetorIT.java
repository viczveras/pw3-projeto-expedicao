package br.edu.ifpb.pweb3.turmalina.app;

import br.edu.ifpb.pweb3.turmalina.dominio.Expedicao;
import br.edu.ifpb.pweb3.turmalina.dominio.Setor;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RemocaoSetorIT extends IntegracaoPostgres {

    @BeforeAll
    void carregarDadosDeExemplo() {
        transacao(DadosExemplo::popular);
    }

    @Test
    void recusaRemoverSetorComColetaRegistrada() {
        Long setorId = idDoSetor("Galeria das Águas");

        PersistenceException erro = assertThrows(PersistenceException.class, () -> removerDaCaverna(setorId));

        exigirSqlState(erro, "23503");
        transacao(em -> {
            assertNotNull(em.find(Setor.class, setorId));
            assertEquals(1L, em.createQuery("select count(c) from Coleta c where c.setor.id = :setor", Long.class)
                    .setParameter("setor", setorId)
                    .getSingleResult());
            return null;
        });
    }

    @Test
    void recusaRemoverSetorAbrangidoPorExpedicao() {
        Long setorId = idDoSetor("Fenda Norte");
        transacao(em -> {
            Expedicao expedicao = em.createQuery("select e from Expedicao e where e.codigo = :codigo", Expedicao.class)
                    .setParameter("codigo", "EXP-2026-003")
                    .getSingleResult();
            expedicao.abrangerSetor(em.find(Setor.class, setorId));
            return null;
        });

        PersistenceException erro = assertThrows(PersistenceException.class, () -> removerDaCaverna(setorId));

        exigirSqlState(erro, "23503");
        transacao(em -> {
            assertNotNull(em.find(Setor.class, setorId));
            return null;
        });
    }

    @Test
    void removeSetorSemColetaNemExpedicao() {
        Long setorId = idDoSetor("Entrada");

        removerDaCaverna(setorId);

        transacao(em -> {
            assertNull(em.find(Setor.class, setorId));
            return null;
        });
    }

    private void removerDaCaverna(Long setorId) {
        transacao(em -> {
            Setor setor = em.find(Setor.class, setorId);
            setor.getCaverna().removerSetor(setor);
            return null;
        });
    }

    private Long idDoSetor(String denominacao) {
        return transacao(em -> em.createQuery("select s.id from Setor s where s.denominacao = :denominacao", Long.class)
                .setParameter("denominacao", denominacao)
                .getSingleResult());
    }
}
