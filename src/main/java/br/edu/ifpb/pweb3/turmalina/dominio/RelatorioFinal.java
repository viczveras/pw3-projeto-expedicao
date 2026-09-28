package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoRelatorio;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Check;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "relatorio_final")
@Check(constraints = "total_paginas > 0")
public class RelatorioFinal extends EntidadeBase {

    @OneToOne(mappedBy = "relatorioFinal", fetch = FetchType.LAZY)
    private Expedicao expedicao;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "text")
    private String resumo;

    @Column(nullable = false)
    private LocalDate dataSubmissao;

    @Column(nullable = false)
    private int totalPaginas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoRelatorio situacaoAprovacao;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "arquivo", nullable = false)
    private byte[] arquivo;

    @Column(nullable = false)
    private boolean publicacaoAutorizada;

    protected RelatorioFinal() {
    }

    public RelatorioFinal(String titulo, String resumo, LocalDate dataSubmissao, int totalPaginas, byte[] arquivo) {
        this.titulo = Objects.requireNonNull(titulo);
        this.resumo = Objects.requireNonNull(resumo);
        this.dataSubmissao = Objects.requireNonNull(dataSubmissao);
        this.totalPaginas = totalPaginas;
        this.arquivo = Objects.requireNonNull(arquivo);
        this.situacaoAprovacao = SituacaoRelatorio.SUBMETIDO;
    }

    void vincular(Expedicao expedicao) {
        if (this.expedicao != null && this.expedicao != expedicao) {
            throw new IllegalStateException("O relatório já pertence a outra expedição");
        }
        this.expedicao = expedicao;
    }

    void desvincular() {
        this.expedicao = null;
    }

    public void aprovar(boolean publicacaoAutorizada) {
        this.situacaoAprovacao = SituacaoRelatorio.APROVADO;
        this.publicacaoAutorizada = publicacaoAutorizada;
    }

    public void reprovar() {
        this.situacaoAprovacao = SituacaoRelatorio.REPROVADO;
        this.publicacaoAutorizada = false;
    }

    public Expedicao getExpedicao() {
        return expedicao;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getResumo() {
        return resumo;
    }

    public LocalDate getDataSubmissao() {
        return dataSubmissao;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public SituacaoRelatorio getSituacaoAprovacao() {
        return situacaoAprovacao;
    }

    public byte[] getArquivo() {
        return arquivo;
    }

    public boolean isPublicacaoAutorizada() {
        return publicacaoAutorizada;
    }
}
