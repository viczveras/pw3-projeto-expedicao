package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoAutorizacao;
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

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "autorizacao_ambiental", uniqueConstraints = @UniqueConstraint(
        name = "uk_autorizacao_orgao_numero", columnNames = {"orgao_emissor", "numero"}))
@Check(constraints = "data_validade >= data_emissao")
public class AutorizacaoAmbiental extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expedicao_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_autorizacao_expedicao"))
    private Expedicao expedicao;

    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @Column(name = "orgao_emissor", nullable = false, length = 100)
    private String orgaoEmissor;

    @Column(nullable = false)
    private LocalDate dataEmissao;

    @Column(nullable = false)
    private LocalDate dataValidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoAutorizacao situacao;

    @Column(length = 1000)
    private String observacoes;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "arquivo_pdf", nullable = false)
    private byte[] arquivoPdf;

    protected AutorizacaoAmbiental() {
    }

    public AutorizacaoAmbiental(String numero, String orgaoEmissor, LocalDate dataEmissao,
                                LocalDate dataValidade, SituacaoAutorizacao situacao, byte[] arquivoPdf) {
        if (dataValidade.isBefore(dataEmissao)) {
            throw new IllegalArgumentException("A validade não pode ser anterior à emissão");
        }
        this.numero = Objects.requireNonNull(numero);
        this.orgaoEmissor = Objects.requireNonNull(orgaoEmissor);
        this.dataEmissao = dataEmissao;
        this.dataValidade = dataValidade;
        this.situacao = Objects.requireNonNull(situacao);
        this.arquivoPdf = Objects.requireNonNull(arquivoPdf);
    }

    void vincular(Expedicao expedicao) {
        if (this.expedicao != null && this.expedicao != expedicao) {
            throw new IllegalStateException("A autorização já pertence a outra expedição");
        }
        this.expedicao = expedicao;
    }

    public boolean validaEm(LocalDate data) {
        return situacao == SituacaoAutorizacao.VIGENTE
                && !data.isBefore(dataEmissao) && !data.isAfter(dataValidade);
    }

    public void revogar(String motivo) {
        this.situacao = SituacaoAutorizacao.REVOGADA;
        this.observacoes = motivo;
    }

    public Expedicao getExpedicao() {
        return expedicao;
    }

    public String getNumero() {
        return numero;
    }

    public String getOrgaoEmissor() {
        return orgaoEmissor;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public SituacaoAutorizacao getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoAutorizacao situacao) {
        this.situacao = Objects.requireNonNull(situacao);
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public byte[] getArquivoPdf() {
        return arquivoPdf;
    }
}
