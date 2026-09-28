# Verificação das entregas de Alan

Datas: 27 e 28/09/2026. Referência: código de Alan na referência técnica da equipe (pacote de 27/09). Os fontes são
incorporados dessa referência etapa por etapa, e não recriados do zero; cada etapa é verificada antes do commit.

## Etapas

| Etapa | Trabalho | Verificação |
|---|---|---|
| 1 | Enums `SituacaoExpedicao`, `PapelParticipante` e `SituacaoAutorizacao` | Valores conferidos contra [diagrama-classes.md](diagrama-classes.md); `./mvnw -B -Pintegracao verify`: 47 testes sem banco e 18 com PostgreSQL aprovados |
| 1 | Relatório técnico (`relatorio-tecnico.md`) | Afirmações sobre o que já existe conferidas contra o código da `main`; trechos que citavam arquivos, classes ou medições ainda inexistentes foram ajustados (abaixo) |
| 2 | `Expedicao`, `PlanoSeguranca`, `AutorizacaoAmbiental` e `Participacao`, junto com as entidades de coleta, amostra, relatório final e movimentação | Com as 14 entidades o esquema é criado sem erro no PostgreSQL; `./mvnw -B -Pintegracao verify`: 47 testes sem banco e 18 com PostgreSQL aprovados |
| 2 | `ExpedicaoTest` (15 testes) e `ExpedicaoIT` (6 testes) | `./mvnw -B -Pintegracao verify`: 62 testes sem banco e 24 com PostgreSQL aprovados; cada regra foi conferida também com uma falha forçada (abaixo) |
| 3 | `orm.xml` com 8 consultas nomeadas, `<mapping-file>` no `persistence.xml` e os DTOs | O `EntityManagerFactory` de todos os testes com banco sobe com as 8 consultas validadas pelo Hibernate; 70 testes sem banco e 27 com PostgreSQL aprovados |
| 3 | `ExpedicaoConsultas`, `ArquivoConsultas` e `ExpedicaoConsultasIT` (6 testes) | Contagem de comandos SQL de cada caso (abaixo); 70 testes sem banco e 33 com PostgreSQL aprovados |

## Ajustes no relatório de referência

O relatório da referência descreve o projeto completo. Nesta etapa ele foi ajustado para não declarar como
pronto o que ainda não existe neste repositório:

- **DDL em arquivo:** dizia que o DDL gerado era gravado em `target/schema-postgresql.sql` a cada execução.
  Nem o `pom.xml`, nem o `persistence.xml`, nem a `Configuracao` configuram essa saída. A frase foi removida.
- **Demonstração, console e evidências:** citava `app.Demonstracao`, `app.ConsoleConsultas`,
  `docs/consultas-ao-vivo.md` e um link para `evidencias-sql.md`, que ainda não existem. O texto passou a
  descrever o que já existe (`Configuracao.propriedadesDaDemonstracao()`, que liga o SQL e as estatísticas)
  e o que será registrado quando as consultas forem incorporadas.
- **Consultas nomeadas:** a contagem de 12 `<named-query>` e 2 `<named-native-query>` e o caso 7
  (indicadores em SQL nativo) dependem de consultas ainda não incorporadas. Foram retirados até existirem;
  o texto mantém a decisão de externalizar as consultas no `orm.xml`.
- **Medições da participação:** "6 SQL para 4 participantes" e "adicionar um participante custa 1 SQL"
  foram medidos fora deste repositório. Viraram meta, comprovada na etapa 2 (ver "Testes do núcleo").
- **Limpeza de objetos grandes:** o texto descrevia a remoção de todos os LOs após a recriação do esquema,
  que no script de referência alcança os LOs do banco inteiro (falha L03). Ficou registrada a regra
  correta, restrita aos objetos da aplicação, que será implementada junto com o `pos-criacao.sql`.

## Testes do núcleo

`ExpedicaoTest` verifica as regras do agregado sem banco: plano obrigatório e vinculado dos dois lados, plano que já
pertence a outra expedição, período e vagas válidos, setores só da caverna da expedição, limite de participantes,
pessoa repetida, confirmação antes da presença, transições de situação (autorização vigente e dentro da validade,
ordem das etapas, cancelamento), uma única autorização vigente, relatório final só após a conclusão e coleções
protegidas.

`ExpedicaoIT` verifica no PostgreSQL:

- plano gravado por cascata, situação gravada como texto (`PLANEJADA`) e tipos nativos: `timestamp without time zone`
  para data e hora, `numeric`, `boolean`, `date` e `oid` para os arquivos binários;
- plano obrigatório e exclusivo: compartilhar o plano de outra expedição viola `uk_expedicao_plano` (23505) e
  retirar o plano viola o `NOT NULL` (23502);
- mesma pessoa duas vezes na mesma expedição rejeitada por `uk_participacao_expedicao_pessoa` (23505);
- adicionar um participante executa 1 comando SQL (a carga das participações), medido com
  `Statistics.getPrepareStatementCount()`, e o limite de participantes continua valendo depois de recarregar;
- transições de situação persistidas e custo negativo rejeitado pela restrição da tabela (23514);
- mapa de rota, PDF da autorização e arquivo do relatório não são carregados junto com a expedição nem com a
  própria entidade; só quando o arquivo é pedido.

Para confirmar que os testes detectam erro de verdade, cada regra foi quebrada temporariamente e o código foi
restaurado em seguida:

| Alteração forçada | Teste que falhou |
|---|---|
| Limite de participantes desligado em `Expedicao.adicionarParticipante` | `ExpedicaoTest.limitaQuantidadeDeParticipantes` |
| `@Basic(fetch = LAZY)` retirado do mapa de rota | `ExpedicaoIT.naoCarregaArquivosBinariosJuntoComAExpedicao` |
| Unicidade (expedição, pessoa) retirada de `Participacao` | `ExpedicaoIT.impedeMesmaPessoaDuasVezesNoPostgres` |

Ficam para as próximas etapas: a autorização vigente única no banco (índice parcial do `pos-criacao.sql`), a troca
de plano rejeitada que desfaz o vínculo antigo (L01) e a remoção de setor com coleta (L02).

## Consultas de expedição e downloads

`ExpedicaoConsultasIT` mede, com `Statistics.getPrepareStatementCount()`, quantos comandos SQL cada consulta executa:

| Caso | Comandos | Verificado também |
|---|---|---|
| 1. Listar expedições por período e situação (projeção `ExpedicaoResumo`) | 1 | filtro de período e de situação, ordem por início, nenhuma entidade no contexto |
| 1b. Anti-exemplo: carregar as expedições e ler o nome da caverna | 1 + 2 | uma consulta extra por caverna distinta (N+1) |
| 2. Detalhes da expedição com participantes e papéis | 2 | caverna e setores carregados; plano, autorizações e participações não carregados |
| 6. Mapa de rota, PDF da autorização vigente, PDF por id, arquivo do relatório e fotografia da amostra | 1 cada | nenhuma entidade no contexto; `Optional.empty` quando o arquivo não existe |

Falhas forçadas, com o código restaurado em seguida:

| Alteração forçada | Teste que falhou |
|---|---|
| `join fetch e.caverna` retirado de `Expedicao.buscarComCavernaESetores` | `ExpedicaoConsultasIT.carregaDetalhesComParticipantesEmDuasConsultasSemArquivos` |
| Mapa de rota lido navegando pela expedição em vez da consulta nomeada | `ExpedicaoConsultasIT.baixaCadaArquivoSeparadamenteComUmaConsultaSemCarregarEntidades` (3 comandos em vez de 1) |

Observação: a listagem de participantes ordena por `papel`. Como o enum é gravado como texto (`EnumType.STRING`),
a ordem é a alfabética do valor gravado (`APOIO_TECNICO`, `COORDENADOR`, `GUIA`...), e não a ordem de declaração.

## Conferido sem ajuste

- Herança JOINED com discriminador `tipo_pessoa` (`PESSOA`, `PESQUISADOR`, `GUIA`) e `pessoa_id` como chave
  primária e estrangeira em `pesquisador` e `guia_espeleologia`.
- `Localizacao` com `numeric(9,6)`, sem setters e com igualdade por valor; `Endereco` incorporado em `pessoa`.
- `Caverna` → `Setor` com `CascadeType.ALL` e `orphanRemoval`, sem coleção de expedições na caverna.
- `EntidadeBase`: identidade `IDENTITY`, `equals` pela raiz da hierarquia lida do proxy sem inicializá-lo e
  `mesmaEntidade` para referências LAZY.
- Tipos já implementados: `numeric(12,2)` no valor de aquisição, `numeric(10,2)` na bolsa, `numeric(8,2)`
  na profundidade, booleanos `nullable = false`, nomes de coluna por `CamelCaseToUnderscoresNamingStrategy`
  e `hibernate-enhance-maven-plugin` no `pom.xml`.

Para repetir a verificação, na raiz do repositório:

```bash
./mvnw -B verify
docker compose --profile test up -d --wait postgres-test
./mvnw -B -Pintegracao verify
```
