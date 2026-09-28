package br.edu.ifpb.pweb3.turmalina.consulta;

import java.util.List;
import java.util.Objects;

import br.edu.ifpb.pweb3.turmalina.consulta.dto.AmostraResumo;
import br.edu.ifpb.pweb3.turmalina.dominio.Coleta;
import jakarta.persistence.EntityManager;

public class ColetaConsultas {

    private final EntityManager em;

    public ColetaConsultas(EntityManager em) {
        this.em = Objects.requireNonNull(em);
    }

    public List<Coleta> listarPorExpedicao(Long expedicaoId) {
        return em.createNamedQuery("Coleta.listarPorExpedicaoComSetorEPesquisador", Coleta.class)
                .setParameter("expedicaoId", expedicaoId)
                .getResultList();
    }

    public List<AmostraResumo> listarAmostras(Long coletaId) {
        return em.createNamedQuery("Amostra.listarResumoPorColeta", AmostraResumo.class)
                .setParameter("coletaId", coletaId)
                .getResultList();
    }
}