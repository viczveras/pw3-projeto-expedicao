package br.edu.ifpb.pweb3.turmalina.consulta;

import java.util.List;
import java.util.Objects;

import br.edu.ifpb.pweb3.turmalina.dominio.MovimentacaoEquipamento;
import jakarta.persistence.EntityManager;

public class EquipamentoConsultas {

    private final EntityManager em;

    public EquipamentoConsultas(EntityManager em) {
        this.em = Objects.requireNonNull(em);
    }

    public List<MovimentacaoEquipamento> listarMovimentacoesPorExpedicao(Long expedicaoId) {
        return em.createNamedQuery("MovimentacaoEquipamento.listarPorExpedicao", MovimentacaoEquipamento.class)
                .setParameter("expedicaoId", expedicaoId)
                .getResultList();
    }
}