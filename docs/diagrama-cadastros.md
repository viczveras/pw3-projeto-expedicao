# UML inicial — parte de Victor

Planejamento para incorporação incremental a partir do setup, registrado antes das classes nesta branch. As classes abaixo representam o escopo planejado, não uma declaração de que todas já foram implementadas. O README informa o estado corrente.

```mermaid
classDiagram
    class EntidadeBase {
        <<MappedSuperclass>>
        -Long id
        +getId() Long
    }
    class Localizacao {
        <<Embeddable>>
        -BigDecimal latitude
        -BigDecimal longitude
        -DatumGeodesico datum
    }
    class Endereco {
        <<Embeddable>>
        -String logradouro
        -String numero
        -String complemento
        -String bairro
        -String cidade
        -UnidadeFederativa uf
        -String cep
    }
    class Caverna {
        -String nomeOficial
        -String codigoCadastroAmbiental
        -String municipio
        -UnidadeFederativa uf
        -BigDecimal altitude
        -BigDecimal extensaoConhecida
        -LocalDate dataUltimaInspecao
        -boolean acessoPermitido
        +adicionarSetor(Setor) Setor
    }
    class Setor {
        -String denominacao
        -NivelDificuldade nivelDificuldade
        -BigDecimal profundidadeMaxima
        -BigDecimal extensaoAproximada
        -String descricao
        -boolean riscoInundacao
        -CondicaoSetor condicaoCorrente
    }
    class Pessoa {
        -String nome
        -String cpf
        -LocalDate dataNascimento
        -String email
        -String telefone
        -boolean ativo
    }
    class Pesquisador {
        -String registroInstitucional
        -String areaPrincipalPesquisa
        -Titulacao titulacao
        -BigDecimal valorDiarioBolsa
    }
    class GuiaEspeleologia {
        -String numeroCredenciamento
        -NivelCertificacao nivelCertificacao
        -LocalDate validadeCertificacao
        -int expedicoesConcluidas
    }
    EntidadeBase <|-- Caverna
    EntidadeBase <|-- Setor
    EntidadeBase <|-- Pessoa
    Pessoa <|-- Pesquisador
    Pessoa <|-- GuiaEspeleologia
    Caverna "1" *-- "1" Localizacao
    Pessoa "1" *-- "1" Endereco
    Caverna "1" *-- "0..*" Setor : setores
    Setor --> Caverna : caverna
```

Decisões herdadas da referência: identidade numérica gerada com IDENTITY; `Localizacao` e `Endereco` incorporados sem tabela própria; enumerações persistidas como STRING; `Setor` proprietário da FK `caverna_id`; cascata e orphanRemoval de Caverna para Setor; herança JOINED planejada para pessoas.

`Pessoa`, suas especializações e sua integração com Alan/Ícaro ficam para uma entrega posterior. O diagrama completo do domínio continua disponível em `docs/diagrama-classes.md` do setup. Este recorte não substitui o UML completo da entrega final.
