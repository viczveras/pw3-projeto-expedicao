package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.valor.Endereco;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "pessoa", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pessoa_cpf", columnNames = "cpf"),
        @UniqueConstraint(name = "uk_pessoa_email", columnNames = "email")})
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_pessoa", discriminatorType = DiscriminatorType.STRING, length = 20)
@DiscriminatorValue("PESSOA")
public class Pessoa extends EntidadeBase {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(nullable = false)
    private boolean ativo;

    @Embedded
    private Endereco endereco;

    protected Pessoa() {
    }

    public Pessoa(String nome, String cpf, LocalDate dataNascimento, String email, String telefone,
                  Endereco endereco) {
        this.nome = Objects.requireNonNull(nome);
        this.cpf = normalizarCpf(cpf);
        this.dataNascimento = Objects.requireNonNull(dataNascimento);
        this.email = Objects.requireNonNull(email).trim().toLowerCase();
        this.telefone = Objects.requireNonNull(telefone);
        this.endereco = Objects.requireNonNull(endereco);
        this.ativo = true;
    }

    private static String normalizarCpf(String cpf) {
        String digitos = Objects.requireNonNull(cpf, "cpf").replaceAll("\\D", "");
        if (digitos.length() != 11) {
            throw new IllegalArgumentException("CPF deve ter 11 dígitos: " + cpf);
        }
        return digitos;
    }

    public void inativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Objects.requireNonNull(nome);
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email).trim().toLowerCase();
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = Objects.requireNonNull(telefone);
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = Objects.requireNonNull(endereco);
    }
}
