# Roteiro de Consultas ao Vivo — Apresentação da Etapa 4

Este guia serve como roteiro para a demonstração prática e explicação das consultas JPQL/SQL durante a apresentação do projeto TurmalinaPB.

---

## 1. Mapeamento e Consultas do `orm.xml`

As consultas de produção estão externalizadas no arquivo `src/main/resources/META-INF/orm.xml` para evitar poluição visual nas entidades Java.

### 1.1 Coletas por Expedição (Fetch Join)
- **Nome da Query:** `Coleta.listarPorExpedicaoComSetorEPesquisador`
- **Técnica:** Utiliza `JOIN FETCH` para carregar as associações `setor` e `pesquisadorResponsavel` em uma única instrução SQL.
- **Objetivo:** Evitar o problema de N+1 ao iterar sobre as coletas de uma expedição, mantendo a coleção de `amostras` carregada de forma *lazy*.

### 1.2 Amostras por Coleta (Projeção DTO)
- **Nome da Query:** `Amostra.listarResumoPorColeta`
- **Técnica:** Projeção direta via `SELECT new br.edu.ifpb.pweb3.turmalina.consulta.dto.AmostraResumo(...)`.
- **Objetivo:** Retornar apenas os atributos necessários para a listagem (código, categoria, quantidade, unidade), ignorando o campo pesado de fotografia (`byte[]`).

### 1.3 Equipamentos Disponíveis no Período (Cláusula NOT EXISTS)
- **Nome da Query:** `Equipamento.listarDisponiveisNoPeriodo`
- **Técnica:** Uso de subquery com `NOT EXISTS` e filtro por estado operacional.
- **Objetivo:** Garantir que o equipamento não possui movimentação ativa no intervalo `[inicio, fim)` e desconsiderar itens com status "aguardando calibração", baixados ou em manutenção (Regra de Negócio D01).

### 1.4 Ranking de Pesquisadores (SQL Nativo + Window Function)
- **Nome da Query:** `Pesquisador.rankingPorAmostras`
- **Técnica:** SQL Nativo PostgreSQL utilizando a função de janela `DENSE_RANK() OVER (ORDER BY COUNT(a.id) DESC)`.
- **Objetivo:** Classificar os pesquisadores de acordo com o volume total de amostras coletadas, tratando empates na mesma posição.

### 1.5 Resumo Financeiro por Caverna (SQL Nativo + Agregação Condicional)
- **Nome da Query:** `Caverna.resumoFinanceiro`
- **Técnica:** SQL Nativo PostgreSQL com agregação condicional via `COUNT(e.id) FILTER (WHERE e.situacao = 'CONCLUIDA')`.
- **Objetivo:** Consolidar estatísticas de orçamento, custo executado e percentual gasto por caverna em um único comando no banco.

---

## 2. Roteiro dos Arquivos Externos na pasta `consultas/`

Os arquivos da pasta `consultas/` servem para testes e execução rápida via console interativo ou DBeaver/pgAdmin.

### `01-distribuicao-por-tipo.jpql`
- **Arquivo:** `consultas/01-distribuicao-por-tipo.jpql`
- **Consulta:** Agrupamento de pessoas utilizando a função JPQL `TYPE(p)` para contar subclasses (Pesquisador, GuiaEspeleologia, etc.).

### `02-maior-orcamento.jpql`
- **Arquivo:** `consultas/02-maior-orcamento.jpql`
- **Consulta:** Utilização do operador JPQL `>= ALL` em subquery para identificar a(s) expedição(ões) com o maior orçamento aprovado.

### `03-pesquisadores-com-coletas.jpql`
- **Arquivo:** `consultas/03-pesquisadores-com-coletas.jpql`
- **Consulta:** Filtro de pesquisadores utilizando a cláusula `EXISTS` vinculada à entidade `Coleta`.

### `04-resumo-financeiro.jpql`
- **Arquivo:** `consultas/04-resumo-financeiro.jpql`
- **Consulta:** Agrupamento por caverna em JPQL calculando a contagem de expedições, soma de orçamentos e custos realizados.

### `05-custo-participantes.jpql`
- **Arquivo:** `consultas/05-custo-participantes.jpql`
- **Consulta:** Projeção do custo previsto de participantes através do cálculo `valorDiaria * quantidadeDiasPrevistos` na entidade `Participacao`.

### `06-colunas-das-tabelas.sql`
- **Arquivo:** `consultas/06-colunas-das-tabelas.sql`
- **Consulta:** SQL Nativo de metadados consultando a tabela de sistema `information_schema.columns` do PostgreSQL para inspecionar a estrutura física do banco.