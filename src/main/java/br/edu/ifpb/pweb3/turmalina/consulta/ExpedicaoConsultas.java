package br.edu.ifpb.pweb3.turmalina.consulta;

import br.edu.ifpb.pweb3.turmalina.consulta.dto.ExpedicaoDetalhe;
import br.edu.ifpb.pweb3.turmalina.consulta.dto.ExpedicaoResumo;
import br.edu.ifpb.pweb3.turmalina.consulta.dto.ParticipanteResumo;
import br.edu.ifpb.pweb3.turmalina.dominio.Expedicao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoExpedicao;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ExpedicaoConsultas {

    private final EntityManager em;

    public ExpedicaoConsultas(EntityManager em) {
        this.em = Objects.requireNonNull(em);
    }

    public List<ExpedicaoResumo> listarPorPeriodoESituacao(LocalDateTime de, LocalDateTime ate,
                                                           Collection<SituacaoExpedicao> situacoes) {
        return em.createNamedQuery("Expedicao.listarPorPeriodoESituacao", ExpedicaoResumo.class)
                .setParameter("de", de)
                .setParameter("ate", ate)
                .setParameter("situacoes", situacoes)
                .getResultList();
    }

    public Optional<ExpedicaoDetalhe> carregarDetalhes(Long expedicaoId) {
        Optional<Expedicao> expedicao = em.createNamedQuery("Expedicao.buscarComCavernaESetores", Expedicao.class)
                .setParameter("expedicaoId", expedicaoId)
                .getResultStream()
                .findFirst();

        return expedicao.map(e -> new ExpedicaoDetalhe(e, listarParticipantes(expedicaoId)));
    }

    public List<ParticipanteResumo> listarParticipantes(Long expedicaoId) {
        return em.createNamedQuery("Participacao.listarResumoPorExpedicao", ParticipanteResumo.class)
                .setParameter("expedicaoId", expedicaoId)
                .getResultList();
    }
}
