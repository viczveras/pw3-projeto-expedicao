package br.edu.ifpb.pweb3.turmalina.dominio.valor;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Objects;

@Embeddable
public class Endereco {

    @Column(name = "logradouro", nullable = false, length = 150)
    private String logradouro;

    @Column(name = "numero", nullable = false, length = 10)
    private String numero;

    @Column(name = "complemento", length = 80)
    private String complemento;

    @Column(name = "bairro", nullable = false, length = 80)
    private String bairro;

    @Column(name = "cidade", nullable = false, length = 100)
    private String cidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "uf", nullable = false, length = 2)
    private UnidadeFederativa uf;

    @Column(name = "cep", nullable = false, length = 8)
    private String cep;

    protected Endereco() {
    }

    public Endereco(String logradouro, String numero, String complemento, String bairro,
                    String cidade, UnidadeFederativa uf, String cep) {
        this.logradouro = Objects.requireNonNull(logradouro, "logradouro");
        this.numero = Objects.requireNonNull(numero, "numero");
        this.complemento = complemento;
        this.bairro = Objects.requireNonNull(bairro, "bairro");
        this.cidade = Objects.requireNonNull(cidade, "cidade");
        this.uf = Objects.requireNonNull(uf, "uf");
        String digitos = Objects.requireNonNull(cep, "cep").replaceAll("\\D", "");
        if (digitos.length() != 8) {
            throw new IllegalArgumentException("CEP deve ter 8 dígitos: " + cep);
        }
        this.cep = digitos;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public UnidadeFederativa getUf() {
        return uf;
    }

    public String getCep() {
        return cep;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Endereco e)) return false;
        return logradouro.equals(e.logradouro) && numero.equals(e.numero)
                && Objects.equals(complemento, e.complemento) && bairro.equals(e.bairro)
                && cidade.equals(e.cidade) && uf == e.uf && cep.equals(e.cep);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logradouro, numero, complemento, bairro, cidade, uf, cep);
    }

    @Override
    public String toString() {
        return logradouro + ", " + numero + (complemento == null ? "" : " " + complemento)
                + " - " + bairro + ", " + cidade + "/" + uf + " - CEP " + cep;
    }
}
