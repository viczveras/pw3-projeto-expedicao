package br.edu.ifpb.pweb3.turmalina.consulta;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import br.edu.ifpb.pweb3.turmalina.dominio.Equipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.MovimentacaoEquipamento;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoOperacional;
import jakarta.persistence.EntityManager;

public class EquipamentoConsultas {

    private static final EnumSet<SituacaoOperacional> FORA_DE_OPERACAO =
            EnumSet.of(SituacaoOperacional.EM_MANUTENCAO, SituacaoOperacional.BAIXADO, SituacaoOperacional.AGUARDANDO_CALIBRACAO);

    private final EntityManager em;

    public EquipamentoConsultas(EntityManager em) {
        this.em = Objects.requireNonNull(em);
    }

    public List<Equipamento> listarDisponiveis(Instant inicio, Instant fim) {
        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException("O fim do intervalo deve ser posterior ao início");
        }
        return em.createNamedQuery("Equipamento.listarDisponiveisNoPeriodo", Equipamento.class)
                .setParameter("foraDeOperacao", FORA_DE_OPERACAO)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .setParameter("agora", Instant.now())
                .getResultList();
    }

    public List<MovimentacaoEquipamento> historico(Long equipamentoId, int pagina, int tamanhoPagina) {
        return em.createNamedQuery("MovimentacaoEquipamento.historicoPorEquipamento", MovimentacaoEquipamento.class)
                .setParameter("equipamentoId", equipamentoId)
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }
}