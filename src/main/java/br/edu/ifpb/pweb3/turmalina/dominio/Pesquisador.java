package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.Titulacao;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Endereco;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "pesquisador", uniqueConstraints = @UniqueConstraint(
        name = "uk_pesquisador_registro", columnNames = "registro_institucional"))
@DiscriminatorValue("PESQUISADOR")
@PrimaryKeyJoinColumn(name = "pessoa_id", foreignKey = @ForeignKey(name = "fk_pesquisador_pessoa"))
@Check(constraints = "valor_diario_bolsa >= 0")
public class Pesquisador extends Pessoa {

    @Column(name = "registro_institucional", nullable = false, length = 30)
    private String registroInstitucional;

    @Column(nullable = false, length = 120)
    private String areaPrincipalPesquisa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Titulacao titulacao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDiarioBolsa;

    protected Pesquisador() {
    }

    public Pesquisador(String nome, String cpf, LocalDate dataNascimento, String email, String telefone,
                       Endereco endereco, String registroInstitucional, String areaPrincipalPesquisa,
                       Titulacao titulacao, BigDecimal valorDiarioBolsa) {
        super(nome, cpf, dataNascimento, email, telefone, endereco);
        this.registroInstitucional = Objects.requireNonNull(registroInstitucional);
        this.areaPrincipalPesquisa = Objects.requireNonNull(areaPrincipalPesquisa);
        this.titulacao = Objects.requireNonNull(titulacao);
        this.valorDiarioBolsa = Objects.requireNonNull(valorDiarioBolsa);
    }

    public String getRegistroInstitucional() {
        return registroInstitucional;
    }

    public String getAreaPrincipalPesquisa() {
        return areaPrincipalPesquisa;
    }

    public void setAreaPrincipalPesquisa(String areaPrincipalPesquisa) {
        this.areaPrincipalPesquisa = Objects.requireNonNull(areaPrincipalPesquisa);
    }

    public Titulacao getTitulacao() {
        return titulacao;
    }

    public void setTitulacao(Titulacao titulacao) {
        this.titulacao = Objects.requireNonNull(titulacao);
    }

    public BigDecimal getValorDiarioBolsa() {
        return valorDiarioBolsa;
    }

    public void setValorDiarioBolsa(BigDecimal valorDiarioBolsa) {
        this.valorDiarioBolsa = Objects.requireNonNull(valorDiarioBolsa);
    }
}
