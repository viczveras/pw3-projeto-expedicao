package br.edu.ifpb.pweb3.turmalina.dominio;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Check;

import java.util.Objects;

@Entity
@Table(name = "plano_seguranca")
@Check(constraints = "tempo_maximo_sem_comunicacao_min > 0")
public class PlanoSeguranca extends EntidadeBase {

    @OneToOne(mappedBy = "planoSeguranca", fetch = FetchType.LAZY)
    private Expedicao expedicao;

    @Column(nullable = false, columnDefinition = "text")
    private String procedimentosEvacuacao;

    @Column(nullable = false, length = 200)
    private String pontoEncontroExterno;

    @Column(name = "tempo_maximo_sem_comunicacao_min", nullable = false)
    private int tempoMaximoSemComunicacaoMin;

    @Column(nullable = false, length = 20)
    private String telefoneEmergencia;

    @Column(nullable = false)
    private boolean necessitaEquipeMedica;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "mapa_rota")
    private byte[] mapaRota;

    protected PlanoSeguranca() {
    }

    public PlanoSeguranca(String procedimentosEvacuacao, String pontoEncontroExterno,
                          int tempoMaximoSemComunicacaoMin, String telefoneEmergencia,
                          boolean necessitaEquipeMedica) {
        this.procedimentosEvacuacao = Objects.requireNonNull(procedimentosEvacuacao);
        this.pontoEncontroExterno = Objects.requireNonNull(pontoEncontroExterno);
        this.tempoMaximoSemComunicacaoMin = tempoMaximoSemComunicacaoMin;
        this.telefoneEmergencia = Objects.requireNonNull(telefoneEmergencia);
        this.necessitaEquipeMedica = necessitaEquipeMedica;
    }

    void vincular(Expedicao expedicao) {
        if (this.expedicao != null && this.expedicao != expedicao) {
            throw new IllegalStateException("O plano de segurança já pertence a outra expedição");
        }
        this.expedicao = expedicao;
    }

    void desvincular() {
        this.expedicao = null;
    }

    public Expedicao getExpedicao() {
        return expedicao;
    }

    public String getProcedimentosEvacuacao() {
        return procedimentosEvacuacao;
    }

    public String getPontoEncontroExterno() {
        return pontoEncontroExterno;
    }

    public int getTempoMaximoSemComunicacaoMin() {
        return tempoMaximoSemComunicacaoMin;
    }

    public String getTelefoneEmergencia() {
        return telefoneEmergencia;
    }

    public boolean isNecessitaEquipeMedica() {
        return necessitaEquipeMedica;
    }

    public byte[] getMapaRota() {
        return mapaRota;
    }

    public void setMapaRota(byte[] mapaRota) {
        this.mapaRota = mapaRota;
    }
}
