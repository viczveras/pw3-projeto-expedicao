package br.edu.ifpb.pweb3.turmalina.consulta;

import java.util.List;
import java.util.Objects;

import br.edu.ifpb.pweb3.turmalina.consulta.dto.RankingPesquisador;
import br.edu.ifpb.pweb3.turmalina.consulta.dto.ResumoCaverna;
import jakarta.persistence.EntityManager;

public class IndicadorConsultas {

    private final EntityManager em;

    public IndicadorConsultas(EntityManager em) {
        this.em = Objects.requireNonNull(em);
    }

    public List<RankingPesquisador> rankingDePesquisadoresPorAmostras() {
        return em.createNamedQuery("Pesquisador.rankingPorAmostras", RankingPesquisador.class).getResultList();
    }

    public List<ResumoCaverna> resumoFinanceiroPorCaverna() {
        return em.createNamedQuery("Caverna.resumoFinanceiro", ResumoCaverna.class).getResultList();
    }
}
