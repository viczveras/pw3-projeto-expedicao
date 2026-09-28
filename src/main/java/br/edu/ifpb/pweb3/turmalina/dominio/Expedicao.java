package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.PapelParticipante;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoAutorizacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoExpedicao;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "expedicao",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_expedicao_codigo", columnNames = "codigo"),
                @UniqueConstraint(name = "uk_expedicao_plano", columnNames = "plano_seguranca_id"),
                @UniqueConstraint(name = "uk_expedicao_relatorio", columnNames = "relatorio_final_id")},
        indexes = @Index(name = "ix_expedicao_periodo_situacao",
                columnList = "inicio_previsto, termino_previsto, situacao"))
@Check(constraints = "termino_previsto > inicio_previsto AND orcamento_aprovado >= 0 "
        + "AND custo_realizado >= 0 AND max_participantes > 0")
public class Expedicao extends EntidadeBase {

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String objetivo;

    @Column(name = "inicio_previsto", nullable = false)
    private LocalDateTime inicioPrevisto;

    @Column(name = "termino_previsto", nullable = false)
    private LocalDateTime terminoPrevisto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal orcamentoAprovado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal custoRealizado = BigDecimal.ZERO;

    @Column(name = "max_participantes", nullable = false)
    private int maxParticipantes;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false, length = 20)
    private SituacaoExpedicao situacao = SituacaoExpedicao.PLANEJADA;

    @Column(nullable = false)
    private boolean cancelamentoEmergencial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caverna_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expedicao_caverna"))
    private Caverna caverna;

    @ManyToMany
    @JoinTable(name = "expedicao_setor",
            joinColumns = @JoinColumn(name = "expedicao_id"),
            inverseJoinColumns = @JoinColumn(name = "setor_id"),
            foreignKey = @ForeignKey(name = "fk_expedicao_setor_expedicao"),
            inverseForeignKey = @ForeignKey(name = "fk_expedicao_setor_setor"))
    private Set<Setor> setores = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "plano_seguranca_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_expedicao_plano"))
    private PlanoSeguranca planoSeguranca;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "relatorio_final_id", foreignKey = @ForeignKey(name = "fk_expedicao_relatorio"))
    private RelatorioFinal relatorioFinal;

    @OneToMany(mappedBy = "expedicao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataEmissao")
    private List<AutorizacaoAmbiental> autorizacoes = new ArrayList<>();

    @OneToMany(mappedBy = "expedicao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participacao> participacoes = new ArrayList<>();

    @OneToMany(mappedBy = "expedicao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataHora")
    private List<Coleta> coletas = new ArrayList<>();

    protected Expedicao() {
    }

    public Expedicao(String codigo, String titulo, String objetivo, Caverna caverna,
                     LocalDateTime inicioPrevisto, LocalDateTime terminoPrevisto,
                     BigDecimal orcamentoAprovado, int maxParticipantes, PlanoSeguranca planoSeguranca) {
        if (!terminoPrevisto.isAfter(inicioPrevisto)) {
            throw new IllegalArgumentException("O término previsto deve ser posterior ao início");
        }
        if (maxParticipantes <= 0) {
            throw new IllegalArgumentException("A expedição deve admitir ao menos um participante");
        }
        this.codigo = Objects.requireNonNull(codigo);
        this.titulo = Objects.requireNonNull(titulo);
        this.objetivo = Objects.requireNonNull(objetivo);
        this.caverna = Objects.requireNonNull(caverna);
        this.inicioPrevisto = inicioPrevisto;
        this.terminoPrevisto = terminoPrevisto;
        this.orcamentoAprovado = Objects.requireNonNull(orcamentoAprovado);
        this.maxParticipantes = maxParticipantes;
        definirPlanoSeguranca(planoSeguranca);
    }

    public void abrangerSetor(Setor setor) {
        if (!mesmaEntidade(setor.getCaverna(), caverna)) {
            throw new IllegalArgumentException("O setor " + setor.getDenominacao()
                    + " não pertence à caverna da expedição");
        }
        setores.add(setor);
    }

    public void deixarDeAbrangerSetor(Setor setor) {
        setores.remove(setor);
    }

    public void definirPlanoSeguranca(PlanoSeguranca plano) {
        Objects.requireNonNull(plano, "A expedição exige um plano de segurança");
        plano.vincular(this);
        if (this.planoSeguranca != null && this.planoSeguranca != plano) {
            this.planoSeguranca.desvincular();
        }
        this.planoSeguranca = plano;
    }

    public AutorizacaoAmbiental registrarAutorizacao(AutorizacaoAmbiental autorizacao) {
        if (autorizacao.getSituacao() == SituacaoAutorizacao.VIGENTE && autorizacaoVigente().isPresent()) {
            throw new IllegalStateException("A expedição já possui uma autorização vigente");
        }
        autorizacao.vincular(this);
        autorizacoes.add(autorizacao);
        return autorizacao;
    }

    public Optional<AutorizacaoAmbiental> autorizacaoVigente() {
        return autorizacoes.stream()
                .filter(a -> a.getSituacao() == SituacaoAutorizacao.VIGENTE)
                .findFirst();
    }

    public Participacao adicionarParticipante(Pessoa pessoa, PapelParticipante papel,
                                              BigDecimal valorDiaria, int diasPrevistos) {
        if (participacoes.size() >= maxParticipantes) {
            throw new IllegalStateException("Limite de " + maxParticipantes + " participantes atingido");
        }

        boolean jaParticipa = participacoes.stream().anyMatch(p -> mesmaEntidade(p.getPessoa(), pessoa));
        if (jaParticipa) {
            throw new IllegalStateException(pessoa.getNome() + " já participa desta expedição");
        }
        Participacao participacao = new Participacao(this, pessoa, papel, valorDiaria, diasPrevistos);
        participacoes.add(participacao);
        return participacao;
    }

    public void removerParticipacao(Participacao participacao) {
        participacoes.remove(participacao);
    }

    public Coleta registrarColeta(Setor setor, Pesquisador responsavel, LocalDateTime dataHora, String metodo) {
        if (setores.stream().noneMatch(s -> mesmaEntidade(s, setor))) {
            throw new IllegalArgumentException("A coleta deve ocorrer em um setor abrangido pela expedição");
        }
        Coleta coleta = new Coleta(this, setor, responsavel, dataHora, metodo);
        coletas.add(coleta);
        return coleta;
    }

    public void anexarRelatorioFinal(RelatorioFinal relatorio) {
        Objects.requireNonNull(relatorio, "Informe o relatório final");
        if (situacao != SituacaoExpedicao.CONCLUIDA) {
            throw new IllegalStateException("O relatório final só pode ser anexado a expedição concluída");
        }
        relatorio.vincular(this);
        if (this.relatorioFinal != null && this.relatorioFinal != relatorio) {
            this.relatorioFinal.desvincular();
        }
        this.relatorioFinal = relatorio;
    }

    public void autorizar(LocalDate hoje) {
        exigirSituacao(SituacaoExpedicao.PLANEJADA);
        AutorizacaoAmbiental vigente = autorizacaoVigente()
                .orElseThrow(() -> new IllegalStateException("Não há autorização ambiental vigente"));
        if (!vigente.validaEm(hoje)) {
            throw new IllegalStateException("A autorização ambiental está fora da validade");
        }
        situacao = SituacaoExpedicao.AUTORIZADA;
    }

    public void iniciar() {
        exigirSituacao(SituacaoExpedicao.AUTORIZADA);
        situacao = SituacaoExpedicao.EM_ANDAMENTO;
    }

    public void concluir(BigDecimal custoRealizado) {
        exigirSituacao(SituacaoExpedicao.EM_ANDAMENTO);
        this.custoRealizado = Objects.requireNonNull(custoRealizado);
        situacao = SituacaoExpedicao.CONCLUIDA;
    }

    public void cancelar(boolean emergencial) {
        if (situacao == SituacaoExpedicao.CONCLUIDA || situacao == SituacaoExpedicao.CANCELADA) {
            throw new IllegalStateException("Expedição " + situacao + " não pode ser cancelada");
        }
        situacao = SituacaoExpedicao.CANCELADA;
        cancelamentoEmergencial = emergencial;
    }

    private void exigirSituacao(SituacaoExpedicao esperada) {
        if (situacao != esperada) {
            throw new IllegalStateException("Operação exige expedição " + esperada + ", mas está " + situacao);
        }
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public LocalDateTime getInicioPrevisto() {
        return inicioPrevisto;
    }

    public LocalDateTime getTerminoPrevisto() {
        return terminoPrevisto;
    }

    public BigDecimal getOrcamentoAprovado() {
        return orcamentoAprovado;
    }

    public BigDecimal getCustoRealizado() {
        return custoRealizado;
    }

    public int getMaxParticipantes() {
        return maxParticipantes;
    }

    public SituacaoExpedicao getSituacao() {
        return situacao;
    }

    public boolean isCancelamentoEmergencial() {
        return cancelamentoEmergencial;
    }

    public Caverna getCaverna() {
        return caverna;
    }

    public Set<Setor> getSetores() {
        return Collections.unmodifiableSet(setores);
    }

    public PlanoSeguranca getPlanoSeguranca() {
        return planoSeguranca;
    }

    public Optional<RelatorioFinal> getRelatorioFinal() {
        return Optional.ofNullable(relatorioFinal);
    }

    public List<AutorizacaoAmbiental> getAutorizacoes() {
        return Collections.unmodifiableList(autorizacoes);
    }

    public List<Participacao> getParticipacoes() {
        return Collections.unmodifiableList(participacoes);
    }

    public List<Coleta> getColetas() {
        return Collections.unmodifiableList(coletas);
    }
}
