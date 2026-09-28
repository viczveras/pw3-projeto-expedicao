package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoOperacional;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.TipoEquipamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "equipamento", uniqueConstraints = @UniqueConstraint(
        name = "uk_equipamento_codigo_patrimonial", columnNames = "codigo_patrimonial"))
@Check(constraints = "valor_aquisicao >= 0")
public class Equipamento extends EntidadeBase {

    @Column(name = "codigo_patrimonial", nullable = false, length = 30)
    private String codigoPatrimonial;

    @Column(nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEquipamento tipo;

    @Column(length = 100)
    private String fabricante;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorAquisicao;

    @Column(nullable = false)
    private LocalDate dataCompra;

    private LocalDate dataUltimaManutencao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SituacaoOperacional situacaoOperacional;

    @Column(nullable = false)
    private boolean exigeCalibracao;

    protected Equipamento() {
    }

    public Equipamento(String codigoPatrimonial, String nome, TipoEquipamento tipo, String fabricante,
                       BigDecimal valorAquisicao, LocalDate dataCompra, boolean exigeCalibracao) {
        this.codigoPatrimonial = Objects.requireNonNull(codigoPatrimonial);
        this.nome = Objects.requireNonNull(nome);
        this.tipo = Objects.requireNonNull(tipo);
        this.fabricante = fabricante;
        this.valorAquisicao = Objects.requireNonNull(valorAquisicao);
        this.dataCompra = Objects.requireNonNull(dataCompra);
        this.exigeCalibracao = exigeCalibracao;
        this.situacaoOperacional = SituacaoOperacional.DISPONIVEL;
    }

    public void registrarManutencao(LocalDate data) {
        this.dataUltimaManutencao = Objects.requireNonNull(data);
        this.situacaoOperacional = SituacaoOperacional.DISPONIVEL;
    }

    public String getCodigoPatrimonial() {
        return codigoPatrimonial;
    }

    public String getNome() {
        return nome;
    }

    public TipoEquipamento getTipo() {
        return tipo;
    }

    public String getFabricante() {
        return fabricante;
    }

    public BigDecimal getValorAquisicao() {
        return valorAquisicao;
    }

    public LocalDate getDataCompra() {
        return dataCompra;
    }

    public LocalDate getDataUltimaManutencao() {
        return dataUltimaManutencao;
    }

    public SituacaoOperacional getSituacaoOperacional() {
        return situacaoOperacional;
    }

    public void setSituacaoOperacional(SituacaoOperacional situacaoOperacional) {
        this.situacaoOperacional = Objects.requireNonNull(situacaoOperacional);
    }

    public boolean isExigeCalibracao() {
        return exigeCalibracao;
    }
}
