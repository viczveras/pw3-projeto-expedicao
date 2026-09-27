package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.CondicaoSetor;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.NivelDificuldade;
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
import java.util.Objects;

@Entity
@Table(name = "setor", uniqueConstraints = @UniqueConstraint(
        name = "uk_setor_caverna_denominacao", columnNames = {"caverna_id", "denominacao"}))
@Check(constraints = "profundidade_maxima >= 0 AND extensao_aproximada >= 0")
public class Setor extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caverna_id", nullable = false, foreignKey = @ForeignKey(name = "fk_setor_caverna"))
    private Caverna caverna;

    @Column(name = "denominacao", nullable = false, length = 100)
    private String denominacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelDificuldade nivelDificuldade;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal profundidadeMaxima;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal extensaoAproximada;

    @Column(length = 1000)
    private String descricao;

    @Column(nullable = false)
    private boolean riscoInundacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CondicaoSetor condicaoCorrente;

    protected Setor() {
    }

    public Setor(String denominacao, NivelDificuldade nivelDificuldade, BigDecimal profundidadeMaxima,
                 BigDecimal extensaoAproximada, boolean riscoInundacao) {
        this.denominacao = Objects.requireNonNull(denominacao);
        this.nivelDificuldade = Objects.requireNonNull(nivelDificuldade);
        this.profundidadeMaxima = Objects.requireNonNull(profundidadeMaxima);
        this.extensaoAproximada = Objects.requireNonNull(extensaoAproximada);
        this.riscoInundacao = riscoInundacao;
        this.condicaoCorrente = CondicaoSetor.EM_AVALIACAO;
    }

    void vincularCaverna(Caverna caverna) {
        if (this.caverna != null && !mesmaEntidade(this.caverna, caverna)) {
            throw new IllegalStateException("Setor já pertence a outra caverna");
        }
        this.caverna = caverna;
    }

    public Caverna getCaverna() {
        return caverna;
    }

    public String getDenominacao() {
        return denominacao;
    }

    public NivelDificuldade getNivelDificuldade() {
        return nivelDificuldade;
    }

    public void setNivelDificuldade(NivelDificuldade nivelDificuldade) {
        this.nivelDificuldade = Objects.requireNonNull(nivelDificuldade);
    }

    public BigDecimal getProfundidadeMaxima() {
        return profundidadeMaxima;
    }

    public BigDecimal getExtensaoAproximada() {
        return extensaoAproximada;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isRiscoInundacao() {
        return riscoInundacao;
    }

    public void setRiscoInundacao(boolean riscoInundacao) {
        this.riscoInundacao = riscoInundacao;
    }

    public CondicaoSetor getCondicaoCorrente() {
        return condicaoCorrente;
    }

    public void setCondicaoCorrente(CondicaoSetor condicaoCorrente) {
        this.condicaoCorrente = Objects.requireNonNull(condicaoCorrente);
    }
}
