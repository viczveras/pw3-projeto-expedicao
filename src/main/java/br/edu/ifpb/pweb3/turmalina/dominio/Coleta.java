package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoValidacaoColeta;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "coleta")
@Check(constraints = "umidade_relativa IS NULL OR (umidade_relativa >= 0 AND umidade_relativa <= 100)")
public class Coleta extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expedicao_id", nullable = false, foreignKey = @ForeignKey(name = "fk_coleta_expedicao"))
    private Expedicao expedicao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "setor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_coleta_setor"))
    private Setor setor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pesquisador_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_coleta_pesquisador"))
    private Pesquisador pesquisadorResponsavel;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 100)
    private String metodo;

    @Column(length = 500)
    private String descricaoPonto;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperatura;

    @Column(name = "umidade_relativa", precision = 5, scale = 2)
    private BigDecimal umidadeRelativa;

    @Column(precision = 8, scale = 2)
    private BigDecimal profundidade;

    @Column(length = 2000)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoValidacaoColeta situacaoValidacao;

    @OneToMany(mappedBy = "coleta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("codigoCampo")
    private List<Amostra> amostras = new ArrayList<>();

    protected Coleta() {
    }

    Coleta(Expedicao expedicao, Setor setor, Pesquisador pesquisadorResponsavel,
           LocalDateTime dataHora, String metodo) {
        this.expedicao = Objects.requireNonNull(expedicao);
        this.setor = Objects.requireNonNull(setor);
        this.pesquisadorResponsavel = Objects.requireNonNull(pesquisadorResponsavel);
        this.dataHora = Objects.requireNonNull(dataHora);
        this.metodo = Objects.requireNonNull(metodo);
        this.situacaoValidacao = SituacaoValidacaoColeta.PENDENTE;
    }

    public void registrarCondicoes(BigDecimal temperatura, BigDecimal umidadeRelativa, BigDecimal profundidade) {
        this.temperatura = temperatura;
        this.umidadeRelativa = umidadeRelativa;
        this.profundidade = profundidade;
    }

    public Amostra adicionarAmostra(Amostra amostra) {
        amostra.vincular(this);
        amostras.add(amostra);
        return amostra;
    }

    public void removerAmostra(Amostra amostra) {
        amostras.remove(amostra);
    }

    public void validar() {
        this.situacaoValidacao = SituacaoValidacaoColeta.VALIDADA;
    }

    public void rejeitar(String motivo) {
        this.situacaoValidacao = SituacaoValidacaoColeta.REJEITADA;
        this.observacoes = motivo;
    }

    public Expedicao getExpedicao() {
        return expedicao;
    }

    public Setor getSetor() {
        return setor;
    }

    public Pesquisador getPesquisadorResponsavel() {
        return pesquisadorResponsavel;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getMetodo() {
        return metodo;
    }

    public String getDescricaoPonto() {
        return descricaoPonto;
    }

    public void setDescricaoPonto(String descricaoPonto) {
        this.descricaoPonto = descricaoPonto;
    }

    public BigDecimal getTemperatura() {
        return temperatura;
    }

    public BigDecimal getUmidadeRelativa() {
        return umidadeRelativa;
    }

    public BigDecimal getProfundidade() {
        return profundidade;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public SituacaoValidacaoColeta getSituacaoValidacao() {
        return situacaoValidacao;
    }

    public List<Amostra> getAmostras() {
        return Collections.unmodifiableList(amostras);
    }
}
