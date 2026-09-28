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

Os arquivos da pasta `consultas/` servem para testes no console interativo.

### `01-expedicoes-por-caverna.jpql`
- **O que faz:** Realiza um `JOIN` entre Expedicao e Caverna, agrupando por caverna para calcular o total de expedições, soma de orçamentos aprovados e soma de custos realizados.

### `02-participantes-da-expedicao.jpql`
- **Uso no console:** Requer o parâmetro `codigo`. Exemplo: `:a 02 codigo=EXP-2026-001`
- **O que faz:** Lista os participantes de uma expedição específica, calculando o custo previsto de cada um (`valorDiaria * quantidadeDiasPrevistos`) e ordenando do maior para o menor custo.

### `03-pessoas-por-tipo.jpql`
- **O que faz:** Utiliza a função `TYPE(p)` para agrupar e contar quantas pessoas existem cadastradas para cada subclasse (Pesquisador, Guia, Apoio Técnico, etc.).

### `04-maior-orcamento.jpql`
- **O que faz:** Utiliza o operador `>= ALL` em uma subquery para encontrar a expedição (ou expedições, em caso de empate) com o maior orçamento aprovado do banco.

### `05-pesquisadores-com-coleta.jpql`
- **O que faz:** Utiliza a cláusula `EXISTS` para listar apenas os pesquisadores que são responsáveis por pelo menos uma coleta, filtrando os inativos.

### `06-colunas-das-tabelas.sql`
- **O que faz:** É uma consulta nativa SQL que acessa o `information_schema.columns` do PostgreSQL para mostrar os metadados físicos (tipo de dado, tamanho, nulidade) da tabela `expedicao`.