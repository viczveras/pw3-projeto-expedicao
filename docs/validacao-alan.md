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
| 3 | `pos-criacao.sql` com a correção da L03 e `ObjetosGrandesIT` (2 testes) | Teste de reprodução falhou com o script da referência e passou após a correção (abaixo); o script roda em todas as classes com banco: 70 testes sem banco e 35 com PostgreSQL aprovados |
| 3 | Autorização vigente única no banco (`ExpedicaoIT`, 1 teste) | Segunda autorização `VIGENTE` da mesma expedição rejeitada pelo índice parcial (23505); 70 testes sem banco e 36 com PostgreSQL aprovados |
| 4 | Correção da L01 em `definirPlanoSeguranca` e `anexarRelatorioFinal` | Testes de reprodução em `ExpedicaoTest` falharam antes da correção e passaram depois (abaixo); `ExpedicaoIT` confirma a remoção do plano antigo por `orphanRemoval`; 73 testes sem banco e 45 com PostgreSQL aprovados |
| 4 | Correção da L02 em `deixarDeAbrangerSetor` | Teste de reprodução em `ExpedicaoTest` falhou antes da correção; `ExpedicaoIT` confirma a regra a partir do banco; 74 testes sem banco e 46 com PostgreSQL aprovados |
| 4 | Revisão final de `relatorio-tecnico.md` | Cada afirmação conferida contra o código e as evidências: 12 consultas JPQL e 2 nativas no `orm.xml`, contagem de comandos de `evidencias-sql.md`, console e roteiro de consultas ao vivo |

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
  correta, restrita aos objetos da aplicação, implementada na etapa 3 (ver "Script pós-criação e falha L03").

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

Ficaram para as etapas seguintes: a autorização vigente única no banco (feita na etapa 3, abaixo), a troca de
plano rejeitada que desfaz o vínculo antigo (L01) e a remoção de setor com coleta (L02).

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

## Script pós-criação e falha L03

O `pos-criacao.sql` da referência cria o índice único parcial `uk_autorizacao_vigente_por_expedicao`, instala a
extensão `lo`, cria os gatilhos `lo_manage` dos quatro arquivos binários e executa
`SELECT lo_unlink(oid) FROM pg_largeobject_metadata`, que apaga todos os objetos grandes do banco.

Sequência que reproduz: criar um objeto grande fora das tabelas da aplicação (`lo_from_bytea`), recriar o
esquema pelo mesmo caminho dos testes e da demonstração (`Configuracao.recriacaoDoEsquema`) e verificar se o
objeto continua existindo. Com o script da referência, `ObjetosGrandesIT.recriarOEsquemaPreservaObjetosGrandesDeForaDaAplicacao`
falhou (`expected: <true> but was: <false>`): o objeto foi apagado. No Docker Compose do projeto o usuário
`turmalina` é superusuário, então o comando alcança os objetos grandes do banco inteiro, e não só os das
tabelas recriadas.

Correção: o comando foi retirado do script. O índice parcial, a extensão e os gatilhos continuam;
`ObjetosGrandesIT.apagaOArquivoAntigoAoSubstituirEAoRemoverALinha` confirma que os gatilhos apagam o arquivo
antigo ao substituir o mapa de rota e ao remover a expedição. Os LOs que ficam órfãos quando o esquema é
recriado são removidos com `vacuumlo`, que só apaga LOs sem nenhuma referência no banco. Executado em modo de
simulação no banco de testes (`vacuumlo -n`), ele listou 11 objetos órfãos deixados por esquemas de teste já
removidos, sem apagar nada.

`ExpedicaoIT.aceitaUmaUnicaAutorizacaoVigentePorExpedicaoNoPostgres` verifica o índice parcial pelo caminho que o
domínio não bloqueia: com uma autorização vigente e outra em análise na mesma expedição, mudar a segunda para
`VIGENTE` com `setSituacao` é rejeitado pelo banco (23505), enquanto outra expedição mantém a sua própria
autorização vigente.

| Alteração forçada | Teste que falhou |
|---|---|
| Índice parcial retirado do script | `ExpedicaoIT.aceitaUmaUnicaAutorizacaoVigentePorExpedicaoNoPostgres` |
| Limpeza global (`lo_unlink` sem filtro) de volta ao script | `ObjetosGrandesIT.recriarOEsquemaPreservaObjetosGrandesDeForaDaAplicacao` |

## Falhas L01 e L02

**L01: troca rejeitada deixava a associação inconsistente.** Sequência: criar duas expedições, cada uma com o
seu plano, e tentar definir na primeira o plano da segunda. `definirPlanoSeguranca` desvinculava o plano atual
antes de o novo recusar o vínculo; a exceção saía, a primeira expedição continuava apontando para o plano antigo,
mas `planoAntigo.getExpedicao()` ficava nulo. `anexarRelatorioFinal` tinha o mesmo padrão, e um relatório nulo
também desfazia o vínculo anterior antes do erro. Antes da correção, `ExpedicaoTest.trocaDePlanoRejeitadaPreservaOsDoisVinculos`
e `ExpedicaoTest.trocaDeRelatorioRejeitadaPreservaOsDoisVinculos` falharam (`expected: <Expedicao#null> but was: <null>`).

Correção: o novo plano (ou relatório) é validado com `vincular` antes de o antigo ser desvinculado, e o relatório
nulo é recusado logo no início. A troca válida continua funcionando (`trocaOPlanoPorOutroAindaSemExpedicao`), e
no PostgreSQL o plano substituído é apagado por `orphanRemoval`
(`ExpedicaoIT.trocarOPlanoApagaOAntigoPorOrphanRemoval`).

**L02: setor com coleta podia deixar a expedição.** Sequência: abranger um setor, registrar uma coleta nele e
chamar `deixarDeAbrangerSetor`. O setor saía da expedição e a coleta ficava num setor que a expedição não abrange
mais, contrariando a regra de que a coleta ocorre em setor abrangido. Antes da correção,
`ExpedicaoTest.naoDeixaDeAbrangerSetorComColetaRegistrada` falhou (nenhuma exceção lançada).

Correção: `deixarDeAbrangerSetor` recusa a remoção enquanto houver coleta naquele setor, comparando as
referências com `mesmaEntidade`, sem inicializar os proxies. Setores sem coleta continuam removíveis.
`ExpedicaoIT.mantemNaExpedicaoOSetorComColetaRegistrada` confirma a regra com os dados carregados do banco. O lado
da caverna (remover da caverna um setor em uso) é recusado pelas chaves estrangeiras, conforme `RemocaoSetorIT`.

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
