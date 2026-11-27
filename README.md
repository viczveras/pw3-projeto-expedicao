# TurmalinaPB Expedições Científicas Subterrâneas

Projeto I da disciplina Programação para a Web 3 (IFPB, Bacharelado em Engenharia de Software). O projeto modela um sistema de expedições científicas em cavernas e faz o mapeamento objeto-relacional desse modelo para o PostgreSQL com Jakarta Persistence.

Equipe: Victor Véras, Alan Borba e Ícaro Pontes.

## Entregáveis

| Entregável pedido no enunciado | Onde está |
|---|---|
| Diagrama de classes UML | [docs/diagrama-classes.md](docs/diagrama-classes.md) |
| Entidades, enumerações e tipos incorporáveis | `src/main/java/br/edu/ifpb/pweb3/turmalina/dominio/` |
| Unidade de persistência e dependências | `src/main/resources/application.yml` e `pom.xml` (o `persistence.xml` da etapa 1 ficou em `src/test/resources/META-INF/`, usado pelos testes com PostgreSQL) |
| Consultas JPA | `src/main/resources/META-INF/orm.xml` e `src/main/java/br/edu/ifpb/pweb3/turmalina/consulta/` |
| SQL gerado pelas consultas | [docs/evidencias-sql.md](docs/evidencias-sql.md) |
| Relatório técnico (herança, dono das associações, cascatas, orphanRemoval e carregamento) | [docs/relatorio-tecnico.md](docs/relatorio-tecnico.md) |

## Tecnologias

- Java 21
- Spring Boot 3.5 (Spring Web, Spring Data JPA e Bean Validation)
- Jakarta Persistence 3.1 com Hibernate ORM 6.6
- springdoc-openapi 2.8 e MapStruct 1.6
- PostgreSQL 17
- Maven 3.9.9, baixado automaticamente pelo Maven Wrapper
- JUnit 5
- Docker Compose e GitHub Actions

## Modelo

O modelo tem 14 entidades: `Caverna`, `Setor`, `Pessoa`, `Pesquisador`, `GuiaEspeleologia`, `Expedicao`, `PlanoSeguranca`, `AutorizacaoAmbiental`, `Participacao`, `Equipamento`, `MovimentacaoEquipamento`, `Coleta`, `Amostra` e `RelatorioFinal`.

- **Herança:** `Pesquisador` e `GuiaEspeleologia` especializam `Pessoa` com a estratégia `JOINED`. Cada especialização tem tabela própria ligada à tabela `pessoa`, e a coluna `tipo_pessoa` identifica o tipo de cada registro.
- **Tipos incorporáveis:** `Localizacao` fica na tabela `caverna`, e `Endereco` fica na tabela `pessoa`. Nenhum dos dois tem identidade ou tabela própria.
- **Associações:** há relações 1:1 (expedição e plano de segurança, expedição e relatório final), 1:N (caverna e setores, expedição e coletas, coleta e amostras) e duas entidades associativas (`Participacao` e `MovimentacaoEquipamento`).
- **Tipos de dados:** as 17 enumerações são gravadas como texto, os campos lógicos são `boolean` do PostgreSQL, as datas usam `java.time`, os valores monetários e as medições usam `numeric` com precisão definida, e os arquivos são objetos grandes (`@Lob`).
- **Integridade:** os identificadores são gerados pelo banco (`IDENTITY`). As regras de obrigatoriedade, tamanho, unicidade e valores válidos estão nas anotações e são criadas como restrições no esquema. O script `META-INF/sql/pos-criacao.sql` cria as regras que o Hibernate não gera, como a de uma única autorização vigente por expedição.

As justificativas de cada decisão estão no [relatório técnico](docs/relatorio-tecnico.md).

## Consultas

### Os seis casos do enunciado

| Caso | Consulta nomeada | Método | Comandos SQL |
|---|---|---|---|
| Expedições por período e situação | `Expedicao.listarPorPeriodoESituacao` | `ExpedicaoConsultas.listarPorPeriodoESituacao` | 1 |
| Detalhes de uma expedição com participantes, sem arquivos | `Expedicao.buscarComCavernaESetores` e `Participacao.listarResumoPorExpedicao` | `ExpedicaoConsultas.carregarDetalhes` | 2 |
| Coletas de uma expedição com setor e pesquisador | `Coleta.listarPorExpedicaoComSetorEPesquisador` | `ColetaConsultas.listarPorExpedicao` | 1 |
| Amostras de uma coleta, ao abrir os detalhes | `Amostra.listarResumoPorColeta` | `ColetaConsultas.listarAmostras` | 1 |
| Equipamentos disponíveis num período, sem carregar o histórico | `Equipamento.listarDisponiveisNoPeriodo` | `EquipamentoConsultas.listarDisponiveis` | 1 |
| Download separado do mapa, da autorização ou do relatório | `PlanoSeguranca.mapaRotaPorExpedicao`, `AutorizacaoAmbiental.pdfPorExpedicaoESituacao` e `RelatorioFinal.arquivoPorExpedicao` | `ArquivoConsultas` | 1 por arquivo |

As listagens usam projeções e `join fetch` para buscar apenas o necessário, e os arquivos só são lidos pelas consultas de download. O SQL gerado em cada caso e a contagem de comandos estão em [docs/evidencias-sql.md](docs/evidencias-sql.md). O mesmo documento mostra, para comparação, uma listagem feita do jeito errado, que provoca o problema N+1.

### Consultas nomeadas no `orm.xml`

As consultas do sistema, executadas pelas classes do pacote `consulta`, estão registradas em `src/main/resources/META-INF/orm.xml`, com o cabeçalho da JPA 3.1, e nenhuma está em anotações nas entidades. São 14 consultas nomeadas: 12 em JPQL e 2 em SQL nativo. As nativas usam recursos do PostgreSQL que a JPQL não oferece: um ranking de pesquisadores com `dense_rank()` e um resumo financeiro por caverna com `count(...) filter (where ...)`. O Spring Boot carrega o `META-INF/orm.xml` automaticamente na subida, e o `persistence.xml` usado pelos testes o declara com `<mapping-file>`.

## Como executar

É preciso ter o JDK 21 e o Docker Desktop. O Maven não precisa ser instalado. Os comandos abaixo são executados na raiz do repositório. No PowerShell, use `.\mvnw.cmd` no lugar de `./mvnw`; para os acentos aparecerem corretamente no terminal do Windows, execute `chcp 65001` antes.

### Testes

```bash
./mvnw -B verify
```

Executa os testes que não usam banco de dados. Para incluir os testes com PostgreSQL:

```bash
docker compose --profile test up -d --wait postgres-test
./mvnw -B -Pintegracao verify
```

São 120 testes: 74 sem banco e 46 com PostgreSQL. Cada classe de teste com banco cria um esquema próprio e o remove ao final. O GitHub Actions executa os mesmos testes a cada push e pull request.

### Executar a aplicação

```bash
docker compose up -d --wait postgres
./mvnw spring-boot:run
```

Sobe a aplicação em `http://localhost:8080`, ligada ao banco de desenvolvimento. Sem apagar dados, as tabelas são criadas ou atualizadas conforme o modelo. A documentação OpenAPI fica em `/swagger-ui.html` e `/v3/api-docs`. Para usar outra porta: `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`. O mesmo pode ser feito dentro de um contêiner com `docker compose --profile app up --build app` (porta definida por `TURMALINA_APP_PORT`).

O perfil `demo` recria o esquema, aplica o script pós-criação e mostra no log o SQL gerado com os parâmetros. **Ele apaga os dados do banco configurado.**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

A demonstração e o console de consultas da etapa 1 continuam disponíveis na tag `etapa-1`.

### Bancos e configuração

| Banco | Endereço | Uso |
|---|---|---|
| Desenvolvimento | `localhost:5434`, base `turmalina` | aplicação e perfil `demo`; os dados ficam no volume `dados-postgres` |
| Testes | `localhost:5435`, base `turmalina_test` | testes com PostgreSQL; os dados ficam só na memória do contêiner |

Para mudar portas e credenciais do Docker Compose, copie `.env.example` para `.env`. Nas execuções locais, a conexão pode ser trocada pelas variáveis `TURMALINA_DB_URL`, `TURMALINA_DB_USER` e `TURMALINA_DB_PASSWORD`. Para desligar os bancos, use `docker compose --profile test down`.

## Estrutura do repositório

```
src/main/java/br/edu/ifpb/pweb3/turmalina/
├── TurmalinaApplication.java  classe principal do Spring Boot
├── dominio/            entidades e a classe base EntidadeBase
│   ├── enums/          enumerações
│   └── valor/          tipos incorporáveis (Localizacao e Endereco)
├── consulta/           classes que executam as consultas nomeadas
│   └── dto/            projeções usadas nas listagens
├── controller/ service/ repository/ dto/ mapper/ exception/ security/ config/
│                       camadas da API REST
└── app/                dados de exemplo
src/main/resources/
├── application.yml     conexão e configuração do JPA
├── application-demo.yml perfil demo
└── META-INF/
    ├── orm.xml         consultas nomeadas
    └── sql/pos-criacao.sql regras criadas depois do esquema
src/test/java/          testes sem banco (*Test) e com PostgreSQL (*IT)
src/test/resources/META-INF/persistence.xml  unidade turmalinaPU usada pelos *IT
consultas/              consultas em arquivo (console da etapa 1)
docs/                   diagramas, relatório, evidências e roteiros
```

## Documentação

- [Diagrama de classes](docs/diagrama-classes.md): entidades, tipos incorporáveis, associações, cardinalidades, enumerações e unicidades.
- [Relatório técnico](docs/relatorio-tecnico.md): justificativas do mapeamento e decisões de domínio.
- [Evidências SQL](docs/evidencias-sql.md): SQL gerado e quantidade de comandos de cada caso do enunciado.
- [Consultas ao vivo](docs/consultas-ao-vivo.md): roteiro de consultas para a apresentação.
- [Diagrama inicial dos cadastros](docs/diagrama-cadastros.md): primeiro recorte do modelo, feito antes da implementação.
- Verificações de cada parte: [Victor](docs/validacao-victor.md) e [Alan](docs/validacao-alan.md), com os testes executados e as falhas encontradas e corrigidas.

## Divisão do trabalho

| Integrante | Parte | Principais itens |
|---|---|---|
| Victor Véras | Cadastros e infraestrutura | `EntidadeBase`, cavernas, setores, pessoas e herança, tipos incorporáveis, unidade de persistência, Docker, testes com PostgreSQL, integração contínua, console de consultas e diagrama de classes |
| Alan Borba | Planejamento | expedição, plano de segurança, autorização ambiental, participação, `orm.xml`, consultas de expedição e de download, script pós-criação e relatório técnico |
| Ícaro Pontes | Operação e resultados | equipamentos e movimentações, coletas, amostras, relatório final, consultas de coletas e equipamentos, indicadores, dados de exemplo, demonstração e evidências SQL |
