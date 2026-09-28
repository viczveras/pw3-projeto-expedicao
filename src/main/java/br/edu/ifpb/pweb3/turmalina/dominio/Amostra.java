package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.CategoriaAmostra;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.CondicaoConservacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeMedida;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "amostra", uniqueConstraints = @UniqueConstraint(
        name = "uk_amostra_codigo_campo", columnNames = "codigo_campo"))
@Check(constraints = "quantidade > 0")
public class Amostra extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coleta_id", nullable = false, foreignKey = @ForeignKey(name = "fk_amostra_coleta"))
    private Coleta coleta;

    @Column(name = "codigo_campo", nullable = false, length = 30)
    private String codigoCampo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaAmostra categoria;

    @Column(nullable = false, precision = 14, scale = 6)
    private BigDecimal quantidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnidadeMedida unidadeMedida;

    @Column(nullable = false)
    private LocalDate dataAcondicionamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CondicaoConservacao condicaoConservacao;

    @Column(nullable = false)
    private boolean materialPerigoso;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "fotografia")
    private byte[] fotografia;

    @Column(length = 1000)
    private String observacoes;

    protected Amostra() {
    }

    public Amostra(String codigoCampo, CategoriaAmostra categoria, BigDecimal quantidade, UnidadeMedida unidadeMedida,
                   LocalDate dataAcondicionamento, CondicaoConservacao condicaoConservacao, boolean materialPerigoso) {
        if (quantidade.signum() <= 0) {
            throw new IllegalArgumentException("A quantidade da amostra deve ser positiva");
        }
        this.codigoCampo = Objects.requireNonNull(codigoCampo);
        this.categoria = Objects.requireNonNull(categoria);
        this.quantidade = quantidade;
        this.unidadeMedida = Objects.requireNonNull(unidadeMedida);
        this.dataAcondicionamento = Objects.requireNonNull(dataAcondicionamento);
        this.condicaoConservacao = Objects.requireNonNull(condicaoConservacao);
        this.materialPerigoso = materialPerigoso;
    }

    void vincular(Coleta coleta) {
        if (this.coleta != null && this.coleta != coleta) {
            throw new IllegalStateException("A amostra já pertence a outra coleta");
        }
        this.coleta = coleta;
    }

    public Coleta getColeta() {
        return coleta;
    }

    public String getCodigoCampo() {
        return codigoCampo;
    }

    public CategoriaAmostra getCategoria() {
        return categoria;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public UnidadeMedida getUnidadeMedida() {
        return unidadeMedida;
    }

    public LocalDate getDataAcondicionamento() {
        return dataAcondicionamento;
    }

    public CondicaoConservacao getCondicaoConservacao() {
        return condicaoConservacao;
    }

    public void setCondicaoConservacao(CondicaoConservacao condicaoConservacao) {
        this.condicaoConservacao = Objects.requireNonNull(condicaoConservacao);
    }

    public boolean isMaterialPerigoso() {
        return materialPerigoso;
    }

    public byte[] getFotografia() {
        return fotografia;
    }

    public void setFotografia(byte[] fotografia) {
        this.fotografia = fotografia;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
