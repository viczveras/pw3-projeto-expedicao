# Roteiro de Consultas ao Vivo - Apresentação (Etapa 4)

Este documento orienta a execução e demonstração das consultas JPA/SQL durante a apresentação do projeto.

## Consultas Mapeadas em `orm.xml`

1. **Coletas por Expedição (Fetch Join):**
   - **JPQL:** `Coleta.listarPorExpedicaoComSetorEPesquisador`
   - **Objetivo:** Traz o setor e o pesquisador responsável em um único `SELECT` via `join fetch`, mantendo a coleção de amostras de forma `LAZY`.
   
2. **Amostras por Coleta (Projeção DTO):**
   - **JPQL:** `Amostra.listarResumoPorColeta`
   - **Objetivo:** Mapeia os dados da amostra diretamente para o DTO `AmostraResumo`, ignorando a coluna binária de fotografia para otimizar a memória.

3. **Equipamentos Disponíveis no Período (Cláusula NOT EXISTS):**
   - **JPQL:** `Equipamento.listarDisponiveisNoPeriodo`
   - **Objetivo:** Valida a disponibilidade de equipamentos desconsiderando itens em manutenção/baixados e garantindo via `NOT EXISTS` no banco que não há reservas conflitantes no intervalo `[inicio, fim)`.

4. **Ranking de Pesquisadores (SQL Nativo + Window Function):**
   - **SQL Nativo:** `Pesquisador.rankingPorAmostras`
   - **Objetivo:** Utiliza a função de janela `DENSE_RANK() OVER (ORDER BY COUNT(a.id) DESC)` do PostgreSQL para classificar os pesquisadores pela quantidade de amostras coletadas.

5. **Resumo Financeiro por Caverna (SQL Nativo + Conditional Aggregation):**
   - **SQL Nativo:** `Caverna.resumoFinanceiro`
   - **Objetivo:** Consolida contagem de expedições e percentual de orçamento executado usando `COUNT(e.id) FILTER (WHERE e.situacao = 'CONCLUIDA')`.