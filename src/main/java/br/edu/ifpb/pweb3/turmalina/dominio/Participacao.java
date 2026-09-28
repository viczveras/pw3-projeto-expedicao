package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.PapelParticipante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "participacao", uniqueConstraints = @UniqueConstraint(
        name = "uk_participacao_expedicao_pessoa", columnNames = {"expedicao_id", "pessoa_id"}))
@Check(constraints = "valor_diaria >= 0 AND quantidade_dias_previstos > 0")
public class Participacao extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expedicao_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_participacao_expedicao"))
    private Expedicao expedicao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false, foreignKey = @ForeignKey(name = "fk_participacao_pessoa"))
    private Pessoa pessoa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PapelParticipante papel;

    private LocalDate dataConfirmacao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDiaria;

    @Column(nullable = false)
    private int quantidadeDiasPrevistos;

    @Column(nullable = false)
    private boolean presencaConfirmada;

    @Column(length = 1000)
    private String observacoes;

    protected Participacao() {
    }

    Participacao(Expedicao expedicao, Pessoa pessoa, PapelParticipante papel,
                 BigDecimal valorDiaria, int quantidadeDiasPrevistos) {
        this.expedicao = Objects.requireNonNull(expedicao);
        this.pessoa = Objects.requireNonNull(pessoa);
        this.papel = Objects.requireNonNull(papel);
        this.valorDiaria = Objects.requireNonNull(valorDiaria);
        this.quantidadeDiasPrevistos = quantidadeDiasPrevistos;
    }

    public void confirmar(LocalDate data) {
        this.dataConfirmacao = Objects.requireNonNull(data);
    }

    public void registrarPresenca() {
        if (dataConfirmacao == null) {
            throw new IllegalStateException("A participação ainda não foi confirmada");
        }
        this.presencaConfirmada = true;
    }

    public BigDecimal custoPrevisto() {
        return valorDiaria.multiply(BigDecimal.valueOf(quantidadeDiasPrevistos));
    }

    public Expedicao getExpedicao() {
        return expedicao;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public PapelParticipante getPapel() {
        return papel;
    }

    public void setPapel(PapelParticipante papel) {
        this.papel = Objects.requireNonNull(papel);
    }

    public LocalDate getDataConfirmacao() {
        return dataConfirmacao;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public int getQuantidadeDiasPrevistos() {
        return quantidadeDiasPrevistos;
    }

    public boolean isPresencaConfirmada() {
        return presencaConfirmada;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
