package br.edu.ifpb.pweb3.turmalina.consulta;

import br.edu.ifpb.pweb3.turmalina.consulta.dto.RankingPesquisador;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Objects;

public class IndicadorConsultas {

    private final EntityManager em;

    public IndicadorConsultas(EntityManager em) {
        this.em = Objects.requireNonNull(em);
    }

    public List<RankingPesquisador> rankingPesquisadoresPorAmostras() {
        return em.createNamedQuery("Pesquisador.rankingPorAmostras", RankingPesquisador.class)
                .getResultList();
    }
}