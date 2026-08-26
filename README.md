# Projeto TurmalinaPB — Sistema de Expedições Científicas Subterrâneas

Este repositório contém a especificação do projeto de **Programação para a Web 3 (IFPB)** para modelagem de um domínio científico subterrâneo com **Java + Jakarta Persistence (JPA) + PostgreSQL**.

## Modalidade e produto

- **Modalidade:** individual ou equipe de até 3 alunos
- **Produto esperado:**
  - modelo Java anotado com JPA
  - esquema relacional PostgreSQL
  - relatório técnico com decisões de modelagem

## Objetivo

Projetar um modelo de objetos para um domínio não trivial e realizar seu mapeamento objeto-relacional com JPA, aplicando corretamente:

- entidades e tipos de valor
- herança
- associações e cardinalidades
- restrições de integridade
- estratégia de carregamento (evitando N+1 e carga excessiva)

## Resumo do domínio

A organização realiza expedições em cavernas e ambientes subterrâneos, envolvendo:

- cadastro de cavernas e setores
- pessoas com especializações (pesquisador e guia)
- planejamento e autorização ambiental da expedição
- participação da equipe com atributos próprios
- equipamentos e movimentações de uso
- coletas científicas, amostras e relatório final

## Regras principais de modelagem

### 1) Cavernas e setores

- Caverna com dados ambientais e geográficos
- Localização como **objeto de valor embutido** (`@Embeddable`)
- Relação **1:N** entre caverna e setor

### 2) Pessoas e herança

- Pessoa base com dados cadastrais + endereço embutido
- Especializações mínimas:
  - Pesquisador
  - Guia de espeleologia
- Escolher e justificar estratégia de herança JPA (`SINGLE_TABLE`, `JOINED` ou `TABLE_PER_CLASS`)

### 3) Expedição e planejamento

- Expedição vinculada a uma caverna e a múltiplos setores
- Expedição com **plano de segurança 1:1 obrigatório**
- Autorização ambiental opcional, com regra de unicidade de autorização vigente por expedição

### 4) Participação da equipe

- Relação pessoa-expedição modelada por **entidade associativa** (não apenas `@ManyToMany`)
- Restrições da participação:
  - papel desempenhado
  - confirmação
  - valores de diária
  - unicidade de pessoa por expedição

### 5) Equipamentos

- Equipamento com código patrimonial único
- Uso modelado por **entidade de movimentação** (equipamento + expedição + responsável)
- Histórico de movimentações **não deve ser carregado automaticamente**

### 6) Coletas, amostras e relatório

- Coleta vinculada à expedição, setor e pesquisador responsável
- Amostras vinculadas à coleta, com código de campo único
- Relatório final 1:1 com expedição
- Arquivos binários (mapa/PDF/foto/relatório) mapeados como LOB, sem Base64 no domínio

## Requisitos técnicos obrigatórios

- IDs numéricos auto incrementáveis compatíveis com PostgreSQL
- Campos obrigatórios com `nullable = false`
- Limites de tamanho coerentes (`length`)
- Unicidade para:
  - CPF
  - código ambiental da caverna
  - código da expedição
  - código patrimonial
  - código de campo da amostra
- Monetários e medições com precisão/escala (`precision`, `scale`)
- Booleanos persistidos como `TRUE/FALSE`
- Tipos `java.time` apropriados para datas e horários
- Enumerações com `@Enumerated(EnumType.STRING)`

## Consultas JPA exigidas

Implementar consultas que evitem N+1 e carregamento excessivo para:

1. listar expedições por período e situação (campos resumidos)
2. carregar detalhes de expedição com participantes e papéis (sem binários)
3. listar coletas de uma expedição com setor e pesquisador
4. carregar amostras sob demanda ao abrir detalhes da coleta
5. consultar equipamentos disponíveis por faixa de datas sem histórico completo
6. baixar separadamente mapa de segurança, autorização ambiental e relatório final

## Entregáveis

- Diagrama UML de classes
- Código-fonte das entidades, enums e `@Embeddable`
- Configuração de persistência e dependências
- Consultas JPA solicitadas
- Relatório técnico justificando:
  - herança
  - ownership de associações
  - cascatas
  - `orphanRemoval`
  - fetch

## Critérios de avaliação

- Fidelidade ao domínio e cardinalidades
- Correção dos mapeamentos e restrições
- Qualidade da estratégia de herança
- Uso correto de enums, binários, tipos temporais e numéricos
- Coerência de cascatas e lados proprietário/inverso
- Eficiência de consultas e carregamento
- Clareza de código e documentação

## Observação

Não há solução única: decisões alternativas são aceitas quando tecnicamente corretas, coerentes com o domínio e devidamente justificadas.
