# Diagrama de classes — TurmalinaPB

Modelo completo do domínio, com as 14 entidades e os 2 tipos incorporáveis. Ele foi registrado neste
repositório antes da implementação das partes de Alan (planejamento) e Ícaro (operação e
resultados); o [README](../README.md) informa o que já está implementado. O recorte inicial dos
cadastros, anterior às classes de Victor, está em [diagrama-cadastros.md](diagrama-cadastros.md).

Diagrama em Mermaid; o GitHub e o VS Code (com extensão de Mermaid) o renderizam direto.
Enumerações aparecem como tipos dos atributos e estão listadas ao final.
`id: Long` (herdado de `EntidadeBase`, `@MappedSuperclass` com identidade IDENTITY) foi omitido das
classes para reduzir o ruído. `[unique]` marca unicidade de uma coluna; as compostas estão ao final.

```mermaid
classDiagram
    direction LR

    class Localizacao {
        <<Embeddable>>
        latitude: BigDecimal
        longitude: BigDecimal
        datum: DatumGeodesico
    }
    class Endereco {
        <<Embeddable>>
        logradouro: String
        numero: String
        complemento: String
        bairro: String
        cidade: String
        uf: UnidadeFederativa
        cep: String
    }

    class Caverna {
        nomeOficial: String
        codigoCadastroAmbiental: String [unique]
        municipio: String
        uf: UnidadeFederativa
        localizacao: Localizacao
        altitude: BigDecimal
        extensaoConhecida: BigDecimal
        dataUltimaInspecao: LocalDate
        acessoPermitido: boolean
    }
    class Setor {
        denominacao: String
        nivelDificuldade: NivelDificuldade
        profundidadeMaxima: BigDecimal
        extensaoAproximada: BigDecimal
        descricao: String
        riscoInundacao: boolean
        condicaoCorrente: CondicaoSetor
    }

    class Pessoa {
        nome: String
        cpf: String [unique]
        dataNascimento: LocalDate
        email: String [unique]
        telefone: String
        ativo: boolean
        endereco: Endereco
    }
    class Pesquisador {
        registroInstitucional: String [unique]
        areaPrincipalPesquisa: String
        titulacao: Titulacao
        valorDiarioBolsa: BigDecimal
    }
    class GuiaEspeleologia {
        numeroCredenciamento: String [unique]
        nivelCertificacao: NivelCertificacao
        validadeCertificacao: LocalDate
        expedicoesConcluidas: int
    }

    class Expedicao {
        codigo: String [unique]
        titulo: String
        objetivo: String
        inicioPrevisto: LocalDateTime
        terminoPrevisto: LocalDateTime
        orcamentoAprovado: BigDecimal
        custoRealizado: BigDecimal
        maxParticipantes: int
        situacao: SituacaoExpedicao
        cancelamentoEmergencial: boolean
    }
    class PlanoSeguranca {
        procedimentosEvacuacao: String
        pontoEncontroExterno: String
        tempoMaximoSemComunicacaoMin: int
        telefoneEmergencia: String
        necessitaEquipeMedica: boolean
        mapaRota: byte[] «Lob, LAZY»
    }
    class AutorizacaoAmbiental {
        numero: String
        orgaoEmissor: String
        dataEmissao: LocalDate
        dataValidade: LocalDate
        situacao: SituacaoAutorizacao
        observacoes: String
        arquivoPdf: byte[] «Lob, LAZY»
    }
    class RelatorioFinal {
        titulo: String
        resumo: String
        dataSubmissao: LocalDate
        totalPaginas: int
        situacaoAprovacao: SituacaoRelatorio
        arquivo: byte[] «Lob, LAZY»
        publicacaoAutorizada: boolean
    }
    class Participacao {
        papel: PapelParticipante
        dataConfirmacao: LocalDate
        valorDiaria: BigDecimal
        quantidadeDiasPrevistos: int
        presencaConfirmada: boolean
        observacoes: String
    }

    class Equipamento {
        codigoPatrimonial: String [unique]
        nome: String
        tipo: TipoEquipamento
        fabricante: String
        valorAquisicao: BigDecimal
        dataCompra: LocalDate
        dataUltimaManutencao: LocalDate
        situacaoOperacional: SituacaoOperacional
        exigeCalibracao: boolean
    }
    class MovimentacaoEquipamento {
        retiradaEm: Instant
        previsaoDevolucao: Instant
        devolucaoEfetiva: Instant
        estadoSaida: EstadoEquipamento
        estadoRetorno: EstadoEquipamento
        custoAvaria: BigDecimal
    }

    class Coleta {
        dataHora: LocalDateTime
        metodo: String
        descricaoPonto: String
        temperatura: BigDecimal
        umidadeRelativa: BigDecimal
        profundidade: BigDecimal
        observacoes: String
        situacaoValidacao: SituacaoValidacaoColeta
    }
    class Amostra {
        codigoCampo: String [unique]
        categoria: CategoriaAmostra
        quantidade: BigDecimal
        unidadeMedida: UnidadeMedida
        dataAcondicionamento: LocalDate
        condicaoConservacao: CondicaoConservacao
        materialPerigoso: boolean
        fotografia: byte[] «Lob, LAZY»
        observacoes: String
    }

    %% Incorporação
    Caverna *-- "1" Localizacao
    Pessoa *-- "1" Endereco

    %% Herança JOINED com discriminador tipo_pessoa e Pessoa concreta
    Pessoa <|-- Pesquisador
    Pessoa <|-- GuiaEspeleologia

    %% Associações (seta = direção de navegação)
    Caverna "1" *-- "0..*" Setor : setores
    Expedicao "0..*" --> "1" Caverna : caverna
    Expedicao "0..*" --> "0..*" Setor : setores
    Expedicao "1" *-- "1" PlanoSeguranca : planoSeguranca
    Expedicao "1" *-- "0..1" RelatorioFinal : relatorioFinal
    Expedicao "1" *-- "0..*" AutorizacaoAmbiental : autorizacoes (≤1 VIGENTE)
    Expedicao "1" *-- "0..*" Participacao : participacoes
    Participacao "0..*" --> "1" Pessoa : pessoa
    Expedicao "1" *-- "0..*" Coleta : coletas
    Coleta "0..*" --> "1" Setor : setor
    Coleta "0..*" --> "1" Pesquisador : pesquisadorResponsavel
    Coleta "1" *-- "0..*" Amostra : amostras
    MovimentacaoEquipamento "0..*" --> "1" Expedicao : expedicao
    MovimentacaoEquipamento "0..*" --> "1" Equipamento : equipamento
    MovimentacaoEquipamento "0..*" --> "1" Pessoa : responsavel
```

## Enumerações

| Enum | Valores |
|---|---|
| SituacaoExpedicao | PLANEJADA, AUTORIZADA, EM_ANDAMENTO, CONCLUIDA, CANCELADA |
| NivelDificuldade | BAIXO, MODERADO, ALTO, EXTREMO |
| CondicaoSetor | LIBERADO, RESTRITO, INTERDITADO, EM_AVALIACAO |
| TipoEquipamento | ILUMINACAO, CORDA_E_ANCORAGEM, PROTECAO_INDIVIDUAL, COMUNICACAO, NAVEGACAO_E_TOPOGRAFIA, MEDICAO_AMBIENTAL, COLETA_DE_AMOSTRAS, PRIMEIROS_SOCORROS, FOTOGRAFIA, OUTRO |
| SituacaoOperacional | DISPONIVEL, EM_USO, EM_MANUTENCAO, AGUARDANDO_CALIBRACAO, BAIXADO |
| EstadoEquipamento | NOVO, BOM, REGULAR, DANIFICADO, INUTILIZAVEL |
| PapelParticipante | COORDENADOR, PESQUISADOR, GUIA, APOIO_TECNICO, SOCORRISTA, FOTOGRAFO, ESTUDANTE |
| CategoriaAmostra | ROCHA, MINERAL, SEDIMENTO, SOLO, AGUA, FAUNA, FLORA, FUNGO, MICROBIOLOGICA, PALEONTOLOGICA, ARQUEOLOGICA |
| CondicaoConservacao | INTEGRA, PARCIALMENTE_DEGRADADA, DEGRADADA, CONTAMINADA |
| UnidadeMedida | MILIGRAMA, GRAMA, QUILOGRAMA, MICROLITRO, MILILITRO, LITRO |
| SituacaoValidacaoColeta | PENDENTE, VALIDADA, REJEITADA |
| SituacaoAutorizacao | EM_ANALISE, VIGENTE, VENCIDA, REVOGADA, INDEFERIDA |
| SituacaoRelatorio | RASCUNHO, SUBMETIDO, EM_REVISAO, APROVADO, REPROVADO |
| Titulacao | GRADUACAO, ESPECIALIZACAO, MESTRADO, DOUTORADO, POS_DOUTORADO |
| NivelCertificacao | BASICO, INTERMEDIARIO, AVANCADO, INSTRUTOR |
| DatumGeodesico | SIRGAS_2000, WGS_84, SAD_69, CORREGO_ALEGRE |
| UnidadeFederativa | 27 siglas |

## Unicidades compostas e regras no banco

| Tabela | Colunas únicas em conjunto | Regra do domínio |
|---|---|---|
| setor | caverna_id, denominacao | Dois setores da mesma caverna não têm o mesmo nome |
| participacao | expedicao_id, pessoa_id | A mesma pessoa não participa duas vezes da mesma expedição |
| autorizacao_ambiental | orgao_emissor, numero | O número da autorização é único por órgão emissor |
| expedicao | plano_seguranca_id; relatorio_final_id | Plano e relatório pertencem a uma única expedição (1:1) |

No máximo uma autorização `VIGENTE` por expedição: índice único parcial criado pelo script
`META-INF/sql/pos-criacao.sql`, executado na criação do esquema.

> Observação: Expedicao → Setor está como `0..*` porque a expedição pode ser planejada antes da
> definição dos setores. Os setores precisam pertencer à caverna da expedição; essa regra é
> verificada em `Expedicao.abrangerSetor`.
