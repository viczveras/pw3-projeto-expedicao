# Relatório técnico — TurmalinaPB

Justificativas das decisões de mapeamento objeto-relacional (enunciado, seção 11).
Pilha: Java 21, Jakarta Persistence 3.1, Hibernate ORM 6.6 e PostgreSQL 17.

## 1. Entidades mapeadas

O modelo tem 14 entidades (o mínimo exigido é 8): Caverna, Setor, Pessoa, Pesquisador, GuiaEspeleologia,
Expedicao, PlanoSeguranca, AutorizacaoAmbiental, Participacao, Equipamento, MovimentacaoEquipamento,
Coleta, Amostra e RelatorioFinal. Há ainda dois tipos incorporáveis: Localizacao e Endereco. O diagrama
completo está em [diagrama-classes.md](diagrama-classes.md).

Nenhum conceito do enunciado foi omitido.

As decisões deste relatório valem para o modelo completo. As 14 entidades, o `orm.xml`, as consultas de
expedição e de arquivos e o `pos-criacao.sql` já estão implementados. As consultas de coletas e de
equipamentos entram nas próximas etapas, e este relatório é atualizado junto com elas.

## 2. Herança: `InheritanceType.JOINED`

| Critério | SINGLE_TABLE | TABLE_PER_CLASS | **JOINED** |
|---|---|---|---|
| NOT NULL nos atributos das subclasses | impossível (colunas de outros tipos ficam nulas) | possível | **possível** |
| FK para "qualquer pessoa" (Participacao, Movimentacao) | possível | **impossível** (não há tabela comum) | **possível** |
| FK só para pesquisador (Coleta) | impossível no banco | possível | **possível** |
| Novo tipo de pessoa | altera a tabela existente | nova tabela | **nova tabela, sem tocar nas existentes** |
| Consulta polimórfica | 1 tabela | UNION ALL | JOINs |

A integridade dos dados foi o que decidiu. O enunciado exige campos essenciais NOT NULL, e o pesquisador
e o guia têm atributos obrigatórios. Além disso, a coleta precisa referenciar especificamente um
**pesquisador**. Com JOINED, `coleta.pesquisador_id` é uma FK para `pesquisador(pessoa_id)`, e o banco
recusa um guia como responsável.

As consultas mais frequentes não precisam das especializações: a listagem de participantes projeta
apenas colunas de `pessoa`. Por isso o custo dos JOINs do JOINED só aparece quando o dado especializado
é realmente pedido. A coluna discriminadora `tipo_pessoa` é opcional em JOINED. Ela foi mantida para
deixar o tipo legível no banco e evitar que o Hibernate precise de `CASE` sobre as subtabelas.

## 3. Tipos incorporáveis

- **Localizacao** (latitude, longitude, datum), incorporada em `caverna`. Usa `numeric(9,6)`:
  6 casas decimais dão resolução de cerca de 0,11 m.
- **Endereco**, incorporado em `pessoa`.

As duas classes são imutáveis, com `equals/hashCode` por valor e sem `@Id`. Para mudar o valor,
substitui-se o objeto inteiro.

## 4. Associações: lado proprietário, cascata e orphanRemoval

| Associação | Cardinalidade | Proprietário (FK) | Cascata | orphanRemoval | Justificativa |
|---|---|---|---|---|---|
| Caverna → Setor | 1 : N bidirecional | Setor (`caverna_id`) | ALL | sim | O setor só existe dentro da caverna (composição). |
| Expedicao → Caverna | N : 1 unidirecional | Expedicao | — | — | A caverna tem vida própria. Não há coleção de expedições na caverna, porque ela só cresceria e nunca é navegada. |
| Expedicao ↔ Setor | N : N unidirecional | Expedicao (`expedicao_setor`) | — | — | O setor pertence à caverna; a expedição só o referencia. |
| Expedicao ↔ PlanoSeguranca | 1 : 1 bidirecional, obrigatória | **Expedicao** (`plano_seguranca_id` NOT NULL UNIQUE) | ALL | sim | Mesmo ciclo de vida. A FK fica na expedição para que o lado da expedição seja LAZY de verdade (ver §5). |
| Expedicao ↔ RelatorioFinal | 1 : 0..1 bidirecional | **Expedicao** (`relatorio_final_id` UNIQUE, anulável) | ALL | sim | Mesma razão; listar expedições não toca em `relatorio_final`. |
| Expedicao ↔ AutorizacaoAmbiental | 1 : N bidirecional | Autorizacao (`expedicao_id`) | ALL | sim | Guarda o histórico. "No máximo uma VIGENTE" é garantido por índice único parcial. |
| Expedicao ↔ Participacao ↔ Pessoa | entidade associativa | Participacao (2 FKs + UNIQUE) | ALL a partir da expedição | sim | Atributos próprios. `uk_participacao_expedicao_pessoa` impede duplicidade. |
| Expedicao ↔ Coleta | 1 : N bidirecional | Coleta | ALL | sim | A coleta é produto da expedição. |
| Coleta → Setor / Pesquisador | N : 1 | Coleta | — | — | Referências a entidades independentes. |
| Coleta ↔ Amostra | 1 : N bidirecional | Amostra | ALL | sim | Composição. |
| Movimentacao → Expedicao / Equipamento / Pessoa | N : 1 (×3) | Movimentacao | — | — | Registro operacional independente. Equipamento **não** tem coleção de movimentações: o histórico é obtido por consulta paginada. |

Não há cascata em direção a entidades com vida própria (Caverna, Setor a partir da expedição, Pessoa,
Equipamento). Se houvesse, remover uma expedição poderia apagar uma caverna ou uma pessoa.

Também não há cascata de Expedicao para MovimentacaoEquipamento. Enquanto houver movimentações, a FK
`fk_movimentacao_expedicao` impede a exclusão da expedição. Isso é intencional: o histórico patrimonial
dos equipamentos não deve sumir junto com a expedição.

Os métodos de domínio (`adicionarSetor`, `adicionarParticipante`, `registrarColeta`, `adicionarAmostra`,
`definirPlanoSeguranca`, ...) mantêm os dois lados das associações bidirecionais sincronizados. Eles
também verificam as regras do agregado: limite de participantes, pessoa duplicada, setor pertencente à
caverna, coleta apenas em setor abrangido e transições de situação.

## 5. Estratégia de carregamento

Regra geral: **todas as associações são LAZY**, inclusive `@ManyToOne` e `@OneToOne`, cujo padrão na
JPA é EAGER. O que cada caso de uso precisa é decidido na consulta (fetch join ou projeção), e não no
mapeamento.

- **@OneToOne e lado proprietário.** No lado *inverso* de um @OneToOne (`mappedBy`), o Hibernate precisa
  consultar a outra tabela para saber se o valor é nulo ou um proxy, então ali LAZY não é garantido.
  Por isso a FK do plano e a do relatório ficam em `expedicao`, e o lado da expedição, que é o navegado
  com frequência, é um proxy LAZY real.
- **Binários** (`mapaRota`, `arquivoPdf`, `fotografia`, `arquivo`) usam `@Lob @Basic(fetch = LAZY)` com
  bytecode enhancement (`hibernate-enhance-maven-plugin`). Sem o enhancement, LAZY em atributo básico é
  só uma dica. Mesmo assim, as consultas da aplicação não dependem disso: usam projeções que não
  selecionam as colunas binárias.
- **Coleções** (`setores`, `participacoes`, `coletas`, `amostras`, `autorizacoes`) são LAZY (padrão JPA).
- **Igualdade e proxies.** Quando a entidade sobrescreve `equals`, chamar `equals()` sobre um proxy LAZY
  faz o Hibernate inicializá-lo. `Hibernate.getClass()`/`getClassLazy()` também inicializam proxies de
  hierarquias. Assim, "a pessoa já participa?" viraria um N+1, com uma consulta por participante já
  cadastrado. Por isso:
  - `EntidadeBase.equals` compara o id e a raiz da hierarquia, lida do proxy sem inicializá-lo;
  - as regras de domínio comparam referências LAZY com `mesmaEntidade` (mesma instância ou mesmo id).

  Resultado: adicionar um participante custa 1 SQL, só para carregar as participações (medido em
  `ExpedicaoIT` com `Statistics.getPrepareStatementCount()`).

## 6. Consultas (enunciado, seção 9)

| # | Caso | Técnica | Comandos SQL esperados |
|---|---|---|---|
| 1 | Listar expedições por período e situação | Projeção `select new ExpedicaoResumo(...)` com `join e.caverna` | 1 |
| 1b | *Anti-exemplo:* `select e from Expedicao e` + `getCaverna().getNomeOficial()` | — | 1 + N (uma por caverna distinta) |
| 2 | Detalhes da expedição com participantes e papéis, sem binários | `join fetch caverna` + `left join fetch setores`, e depois projeção `ParticipanteResumo` (sem endereço nem subtabelas) | 2 |
| 3 | Coletas com setor e pesquisador | `join fetch c.setor join fetch c.pesquisadorResponsavel` | 1 |
| 4 | Amostras apenas ao abrir a coleta | Consulta separada, projeção sem a fotografia | 1 |
| 5 | Equipamentos disponíveis numa faixa de datas | `NOT EXISTS` sobre movimentações, resolvido no banco com índice | 1 |
| 6 | Baixar mapa, autorização ou relatório | `select p.mapaRota ...` / `select a.arquivoPdf ...` / `select r.arquivo ...`: só a coluna do LOB | 1 cada |

A contagem dos casos 1, 1b, 2 e 6 é conferida em `ExpedicaoConsultasIT` com
`Statistics.getPrepareStatementCount()`, que também confirma que as projeções e os downloads não colocam
nenhuma entidade no contexto de persistência. A demonstração vai executar cada caso com
`Configuracao.propriedadesDaDemonstracao()`, que liga `hibernate.show_sql` e as estatísticas do Hibernate.

### Consultas nomeadas externalizadas (`orm.xml`)

O texto das consultas fica em `META-INF/orm.xml` (cabeçalho JPA 3.1), declarado no `persistence.xml`
com `<mapping-file>`. Isso também atende ao pedido de registrar pelo menos duas consultas nomeadas em
`orm.xml`. As classes de `consulta/` só chamam `createNamedQuery("<Entidade>.<finalidade>", Tipo.class)`.

Motivos:
- **Separação:** o texto da consulta fica fora do código, então é possível revisá-lo e ajustá-lo sem
  mexer na lógica Java.
- **Validação antecipada:** o Hibernate valida todas as consultas JPQL nomeadas ao criar o
  `EntityManagerFactory`. Um erro de sintaxe ou um atributo inexistente impede a aplicação de subir, em
  vez de falhar só quando a consulta é usada.

### Evidências

O SQL gerado em cada caso, com a contagem de comandos e o resultado, será registrado em
`evidencias-sql.md` quando as consultas forem incorporadas. O contraste principal é entre o caso 1
(projeção: 1 comando) e o anti-exemplo 1b (entidades com acesso LAZY à caverna: 1 + N comandos).

## 7. Tipos e restrições

- **Identificadores:** `GenerationType.IDENTITY`, que gera `bigint generated by default as identity`
  no PostgreSQL. Efeito colateral conhecido: o Hibernate não agrupa INSERTs em lote (JDBC batch).
- **Enumerações:** `EnumType.STRING`, com valor legível e estável se a ordem das constantes mudar. O
  Hibernate 6 também gera `CHECK (col in (...))`.
- **Booleanos:** `boolean` primitivo com `nullable = false`, persistido como `boolean` nativo do PostgreSQL.
- **Dinheiro:** `numeric(12,2)` para orçamento, custo e valor de aquisição; `numeric(10,2)` para diárias,
  bolsa e avaria.
- **Medições:** `numeric(14,6)` para massa/volume, `numeric(5,2)` para temperatura e umidade,
  `numeric(8,2)` para profundidade, e `numeric(9,6)` para coordenadas.
- **Datas:** `LocalDate` para datas sem horário (nascimento, emissão, validade, compra). `LocalDateTime`
  para data e hora locais de agenda ou campo (início e término previstos, coleta). `Instant` para
  instantes de eventos (retirada e devolução de equipamento), gravado como `timestamptz`, o que torna a
  verificação de sobreposição independente de fuso.
- **Binários:** `@Lob byte[]`. No PostgreSQL, o Hibernate 6 mapeia para `oid` (Large Object), sem Base64
  no domínio. Para não deixar objetos grandes (LOs) órfãos, o `pos-criacao.sql` instala a extensão `lo`
  e gatilhos `lo_manage`, que removem o LO quando a linha é apagada ou o arquivo é substituído. O script
  não apaga objetos grandes em massa: recriar o esquema apaga as tabelas sem disparar os gatilhos, e os LOs
  dessas linhas ficam órfãos. Eles são removidos com `vacuumlo`, ferramenta do PostgreSQL que só apaga os
  LOs que nenhuma coluna do banco referencia (por exemplo,
  `docker compose exec postgres vacuumlo -U turmalina turmalina`).
- **Unicidade:** CPF, e-mail, código ambiental da caverna, código da expedição, código patrimonial,
  código de campo da amostra, registro do pesquisador, credenciamento do guia, (caverna, denominação do
  setor), (órgão, número da autorização), (expedição, pessoa) na participação, e as FKs 1:1 da expedição.
- **CHECKs** (`@org.hibernate.annotations.Check`): término > início, valores não negativos, umidade entre
  0 e 100, validade ≥ emissão, devolução ≥ retirada, quantidade de amostra > 0, entre outros.
- **Restrição fora das anotações:** o índice único parcial `uk_autorizacao_vigente_por_expedicao`
  (`WHERE situacao = 'VIGENTE'`) não é expressável em JPA e fica no `pos-criacao.sql`, executado depois
  da criação das tabelas.
