package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Localizacao;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "caverna", uniqueConstraints = @UniqueConstraint(
        name = "uk_caverna_codigo_ambiental", columnNames = "codigo_cadastro_ambiental"))
@Check(constraints = "extensao_conhecida IS NULL OR extensao_conhecida >= 0")
public class Caverna extends EntidadeBase {

    @Column(nullable = false, length = 150)
    private String nomeOficial;

    @Column(name = "codigo_cadastro_ambiental", nullable = false, length = 30)
    private String codigoCadastroAmbiental;

    @Column(nullable = false, length = 100)
    private String municipio;

    @Enumerated(EnumType.STRING)
    @Column(name = "uf", nullable = false, length = 2)
    private UnidadeFederativa uf;

    @Embedded
    private Localizacao localizacao;

    @Column(precision = 7, scale = 2)
    private BigDecimal altitude;

    @Column(precision = 10, scale = 2)
    private BigDecimal extensaoConhecida;

    private LocalDate dataUltimaInspecao;

    @Column(nullable = false)
    private boolean acessoPermitido;

    @OneToMany(mappedBy = "caverna", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("denominacao")
    private List<Setor> setores = new ArrayList<>();

    protected Caverna() {
    }

    public Caverna(String nomeOficial, String codigoCadastroAmbiental, String municipio,
                   UnidadeFederativa uf, Localizacao localizacao) {
        this.nomeOficial = Objects.requireNonNull(nomeOficial);
        this.codigoCadastroAmbiental = Objects.requireNonNull(codigoCadastroAmbiental);
        this.municipio = Objects.requireNonNull(municipio);
        this.uf = Objects.requireNonNull(uf);
        this.localizacao = Objects.requireNonNull(localizacao);
        this.acessoPermitido = true;
    }

    public Setor adicionarSetor(Setor setor) {
        Objects.requireNonNull(setor, "setor");
        if (setores.stream().anyMatch(existente -> mesmaEntidade(existente, setor))) {
            throw new IllegalStateException("O setor já está cadastrado nesta caverna");
        }
        setor.vincularCaverna(this);
        setores.add(setor);
        return setor;
    }

    public void removerSetor(Setor setor) {
        setores.remove(setor);
    }

    public void registrarInspecao(LocalDate data, boolean acessoPermitido) {
        this.dataUltimaInspecao = Objects.requireNonNull(data);
        this.acessoPermitido = acessoPermitido;
    }

    public String getNomeOficial() {
        return nomeOficial;
    }

    public String getCodigoCadastroAmbiental() {
        return codigoCadastroAmbiental;
    }

    public String getMunicipio() {
        return municipio;
    }

    public UnidadeFederativa getUf() {
        return uf;
    }

    public Localizacao getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(Localizacao localizacao) {
        this.localizacao = Objects.requireNonNull(localizacao);
    }

    public BigDecimal getAltitude() {
        return altitude;
    }

    public void setAltitude(BigDecimal altitude) {
        this.altitude = altitude;
    }

    public BigDecimal getExtensaoConhecida() {
        return extensaoConhecida;
    }

    public void setExtensaoConhecida(BigDecimal extensaoConhecida) {
        this.extensaoConhecida = extensaoConhecida;
    }

    public LocalDate getDataUltimaInspecao() {
        return dataUltimaInspecao;
    }

    public boolean isAcessoPermitido() {
        return acessoPermitido;
    }

    public List<Setor> getSetores() {
        return Collections.unmodifiableList(setores);
    }
}
