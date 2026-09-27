package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.NivelCertificacao;
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

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "guia_espeleologia", uniqueConstraints = @UniqueConstraint(
        name = "uk_guia_credenciamento", columnNames = "numero_credenciamento"))
@DiscriminatorValue("GUIA")
@PrimaryKeyJoinColumn(name = "pessoa_id", foreignKey = @ForeignKey(name = "fk_guia_pessoa"))
@Check(constraints = "expedicoes_concluidas >= 0")
public class GuiaEspeleologia extends Pessoa {

    @Column(name = "numero_credenciamento", nullable = false, length = 30)
    private String numeroCredenciamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelCertificacao nivelCertificacao;

    @Column(nullable = false)
    private LocalDate validadeCertificacao;

    @Column(nullable = false)
    private int expedicoesConcluidas;

    protected GuiaEspeleologia() {
    }

    public GuiaEspeleologia(String nome, String cpf, LocalDate dataNascimento, String email, String telefone,
                            Endereco endereco, String numeroCredenciamento, NivelCertificacao nivelCertificacao,
                            LocalDate validadeCertificacao) {
        super(nome, cpf, dataNascimento, email, telefone, endereco);
        this.numeroCredenciamento = Objects.requireNonNull(numeroCredenciamento);
        this.nivelCertificacao = Objects.requireNonNull(nivelCertificacao);
        this.validadeCertificacao = Objects.requireNonNull(validadeCertificacao);
    }

    public boolean certificacaoValidaEm(LocalDate data) {
        return !data.isAfter(validadeCertificacao);
    }

    public void renovarCertificacao(NivelCertificacao nivel, LocalDate novaValidade) {
        this.nivelCertificacao = Objects.requireNonNull(nivel);
        this.validadeCertificacao = Objects.requireNonNull(novaValidade);
    }

    public void registrarExpedicaoConcluida() {
        expedicoesConcluidas++;
    }

    public String getNumeroCredenciamento() {
        return numeroCredenciamento;
    }

    public NivelCertificacao getNivelCertificacao() {
        return nivelCertificacao;
    }

    public LocalDate getValidadeCertificacao() {
        return validadeCertificacao;
    }

    public int getExpedicoesConcluidas() {
        return expedicoesConcluidas;
    }
}
