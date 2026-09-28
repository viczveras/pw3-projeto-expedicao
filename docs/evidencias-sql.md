# Evidências de Execução das Consultas (Etapa 4) - TurmalinaPB

Abaixo estão registrados os logs obtidos na execução do programa principal de demonstração (`br.edu.ifpb.pweb3.turmalina.app.Demonstracao`), validando a execução eficiente das NamedQueries JPA e SQL nativas.

## Logs de Saída do Terminal

```text
====================================================================================================
Carga de dados de exemplo
====================================================================================================

====================================================================================================
1. Listar expedições por período e situação (projeção)
====================================================================================================
   EXP-2026-001 | Levantamento bioespeleológico da Galeria das Águas | Gruta da Serra Verde | 2026-08-10T07:00 -> 2026-08-15T18:00 | CONCLUIDA
   EXP-2026-002 | Mapeamento topográfico da Furna do Lajedo | Furna do Lajedo | 2026-10-05T07:00 -> 2026-10-09T17:00 | PLANEJADA
   EXP-2026-003 | Monitoramento hídrico da Galeria das Águas | Gruta da Serra Verde | 2026-11-03T06:00 -> 2026-11-06T18:00 | PLANEJADA

====================================================================================================
1b. ANTI-EXEMPLO: entidades + acesso à caverna (N+1)
====================================================================================================
   EXP-2026-001 | Gruta da Serra Verde
   EXP-2026-002 | Furna do Lajedo
   EXP-2026-003 | Gruta da Serra Verde

====================================================================================================
2. Detalhes de uma expedição com participantes (sem binários)
====================================================================================================
   EXP-2026-001 - Levantamento bioespeleológico da Galeria das Águas (Gruta da Serra Verde), setores: 2
   - APOIO_TECNICO: Ravi Medeiros
   - COORDENADOR: Ana Beatriz Lima
   - GUIA: Maria Eduarda Souto
   - PESQUISADOR: Caio Nóbrega

====================================================================================================
3. Coletas de uma expedição com setor e pesquisador (fetch join)
====================================================================================================
   2026-08-11T10:30 | Galeria das Águas | Ana Beatriz Lima | Busca ativa com pinça
   2026-08-12T14:00 | Salão Principal | Caio Nóbrega | Testemunho de sedimento

====================================================================================================
4. Amostras de uma coleta, ao abrir os detalhes (sem fotografia)
====================================================================================================
   GA-0001 | FAUNA | 0.004250 g
   GA-0002 | AGUA | 250.000000 mL

====================================================================================================
5. Equipamentos disponíveis nos próximos 7 dias (NOT EXISTS, sem histórico)
====================================================================================================
   PAT-000103 | Rádio subterrâneo HeyPhone
   PAT-000101 | Lanterna de cabeça 1200 lm

====================================================================================================
6. Download isolado dos arquivos binários
====================================================================================================
   mapa de rota:     17 bytes
   autorização PDF:  20 bytes
   relatório final:  24 bytes

====================================================================================================
7. Consultas nativas externalizadas (recursos do PostgreSQL)
====================================================================================================
   Ranking de pesquisadores (dense_rank):
   1. Ana Beatriz Lima | coletas: 1 | amostras: 2
   2. Caio Nóbrega | coletas: 1 | amostras: 1
   Resumo financeiro por caverna (COUNT ... FILTER):
   Furna do Lajedo | expedições: 1 (concluídas: 0) | orçamento: 12000.00 | custo: 0.00 | executado: 0.0%
   Gruta da Serra Verde | expedições: 2 (concluídas: 1) | orçamento: 53000.00 | custo: 31280.55 | executado: 59.0%