package br.edu.ifpb.pweb3.turmalina.consulta;

import br.edu.ifpb.pweb3.turmalina.consulta.dto.AmostraResumo;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;

public class ColetaConsultas {

    private final EntityManager em;

    public ColetaConsultas(EntityManager em) {
        this.em = Objects.requireNonNull(em);
    }

    public List<AmostraResumo> listarAmostrasPorExpedicao(Long expedicaoId) {
        return em.createNamedQuery("Coleta.listarResumoAmostrasPorExpedicao", AmostraResumo.class)
                .setParameter("expedicaoId", expedicaoId)
                .getResultList();
    }

    public byte[] buscarFotografiaAmostra(Long amostraId) {
        List<byte[]> resultado = em.createNamedQuery("Amostra.fotografiaPorId", byte[].class)
                .setParameter("amostraId", amostraId)
                .getResultList();
        return resultado.isEmpty() ? null : resultado.get(0);
    }
}