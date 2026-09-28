# Verificação das entregas de Alan

Data: 27/09/2026. Referência: código de Alan na referência técnica da equipe (pacote de 27/09). Os fontes são
incorporados dessa referência etapa por etapa, e não recriados do zero; cada etapa é verificada antes do commit.

## Etapas

| Etapa | Trabalho | Verificação |
|---|---|---|
| 1 | Enums `SituacaoExpedicao`, `PapelParticipante` e `SituacaoAutorizacao` | Valores conferidos contra [diagrama-classes.md](diagrama-classes.md); `./mvnw -B -Pintegracao verify`: 47 testes sem banco e 18 com PostgreSQL aprovados |
| 1 | Relatório técnico (`relatorio-tecnico.md`) | Afirmações sobre o que já existe conferidas contra o código da `main`; trechos que citavam arquivos, classes ou medições ainda inexistentes foram ajustados (abaixo) |

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
  foram medidos fora deste repositório. Viraram meta, a ser comprovada no teste de integração da expedição.
- **Limpeza de objetos grandes:** o texto descrevia a remoção de todos os LOs após a recriação do esquema,
  que no script de referência alcança os LOs do banco inteiro (falha L03). Ficou registrada a regra
  correta, restrita aos objetos da aplicação, que será implementada junto com o `pos-criacao.sql`.

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
