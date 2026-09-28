package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.EstadoEquipamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "movimentacao_equipamento",
        indexes = @Index(name = "ix_movimentacao_equipamento_periodo",
                columnList = "equipamento_id, retirada_em, previsao_devolucao"))
@Check(constraints = "previsao_devolucao > retirada_em "
        + "AND (devolucao_efetiva IS NULL OR devolucao_efetiva >= retirada_em) "
        + "AND (custo_avaria IS NULL OR custo_avaria >= 0)")
public class MovimentacaoEquipamento extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expedicao_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_movimentacao_expedicao"))
    private Expedicao expedicao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipamento_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_movimentacao_equipamento"))
    private Equipamento equipamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_movimentacao_responsavel"))
    private Pessoa responsavel;

    @Column(name = "retirada_em", nullable = false)
    private Instant retiradaEm;

    @Column(name = "previsao_devolucao", nullable = false)
    private Instant previsaoDevolucao;

    @Column(name = "devolucao_efetiva")
    private Instant devolucaoEfetiva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEquipamento estadoSaida;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoEquipamento estadoRetorno;

    @Column(name = "custo_avaria", precision = 10, scale = 2)
    private BigDecimal custoAvaria;

    protected MovimentacaoEquipamento() {
    }

    public MovimentacaoEquipamento(Expedicao expedicao, Equipamento equipamento, Pessoa responsavel,
                                   Instant retiradaEm, Instant previsaoDevolucao, EstadoEquipamento estadoSaida) {
        if (!previsaoDevolucao.isAfter(retiradaEm)) {
            throw new IllegalArgumentException("A previsão de devolução deve ser posterior à retirada");
        }
        this.expedicao = Objects.requireNonNull(expedicao);
        this.equipamento = Objects.requireNonNull(equipamento);
        this.responsavel = Objects.requireNonNull(responsavel);
        this.retiradaEm = retiradaEm;
        this.previsaoDevolucao = previsaoDevolucao;
        this.estadoSaida = Objects.requireNonNull(estadoSaida);
    }

    public void registrarDevolucao(Instant quando, EstadoEquipamento estadoRetorno, BigDecimal custoAvaria) {
        if (devolucaoEfetiva != null) {
            throw new IllegalStateException("Devolução já registrada");
        }
        if (quando.isBefore(retiradaEm)) {
            throw new IllegalArgumentException("A devolução não pode ser anterior à retirada");
        }
        this.devolucaoEfetiva = quando;
        this.estadoRetorno = Objects.requireNonNull(estadoRetorno);
        this.custoAvaria = custoAvaria;
    }

    public boolean isDevolvido() {
        return devolucaoEfetiva != null;
    }

    public Expedicao getExpedicao() {
        return expedicao;
    }

    public Equipamento getEquipamento() {
        return equipamento;
    }

    public Pessoa getResponsavel() {
        return responsavel;
    }

    public Instant getRetiradaEm() {
        return retiradaEm;
    }

    public Instant getPrevisaoDevolucao() {
        return previsaoDevolucao;
    }

    public Instant getDevolucaoEfetiva() {
        return devolucaoEfetiva;
    }

    public EstadoEquipamento getEstadoSaida() {
        return estadoSaida;
    }

    public EstadoEquipamento getEstadoRetorno() {
        return estadoRetorno;
    }

    public BigDecimal getCustoAvaria() {
        return custoAvaria;
    }
}
