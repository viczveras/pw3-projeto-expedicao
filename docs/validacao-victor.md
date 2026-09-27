# Verificação das entregas de Victor

Data: 27/09/2026. Referência: setup no commit `84b288071bab8af9867a789b1d681e8c9cf621c8`, pasta `organizacao-equipe/modulos/victor/codigo/`. Os fontes foram incorporados dessa referência, e não recriados do zero.

## Etapas

| Etapa | Trabalho | Verificação |
|---|---|---|
| 1 | Maven, README, guia da equipe e UML planejado | `mvn -B verify`: build aprovado; ainda sem classes/testes |
| 2 | Identidade, Endereco, Localizacao, UF e datum | `mvn -B verify`: 18 testes aprovados |
| 3 | Caverna, Setor, enums e correção de duplicidade | Teste de regressão reproduziu a falha no setup; `mvn -B verify`: 24 testes aprovados após a correção |
| 4 | Estrutura base: Maven Wrapper, `persistence.xml`, `Configuracao`, `Inicializacao`, Docker Compose, Dockerfile e CI | `./mvnw -B -Pintegracao verify`: 27 testes sem banco e 5 de integração aprovados; `docker compose run --rm --build app` e `./mvnw compile exec:java` conectaram ao PostgreSQL 17 |

## Ajuste real encontrado na referência

Sequência: criar uma caverna e um setor, adicionar o setor e tentar adicionar novamente a mesma instância. O setup chama `setores.add` novamente e passa a apresentar duas entradas para o mesmo objeto.

O teste `CavernaTest.rejeitaAdicionarMesmoSetorDuasVezes` foi executado antes da correção com `mvn -B -Dtest=CavernaTest test`. O resultado foi 6 testes, 1 falha: era esperada `IllegalStateException`, mas nenhuma exceção foi lançada. Não houve inserção proposital de defeito; o comportamento já existia no código de referência.

O ajuste rejeita a repetição antes de modificar o vínculo. A comparação usa a identidade da entidade, conforme o contrato já fornecido por `EntidadeBase.mesmaEntidade`. O teste exige que, após a tentativa rejeitada, a coleção continue com um único setor e o vínculo original permaneça intacto. Entidades distintas com a mesma denominação continuam sujeitas à restrição relacional do setup; esta correção trata a repetição da mesma entidade na coleção.

## Escopo verificado

- Localizacao: quatro extremos válidos, quatro coordenadas fora do limite, igualdade com escalas decimais diferentes, datum e campos obrigatórios.
- Endereco: CEP normalizado com zero inicial, tamanho inválido, ausência de CEP e igualdade/diferença de complementos.
- Caverna/Setor: vínculo dos dois lados, rejeição de transferência, proteção da coleção, inspeção, identidade de setores transientes e rejeição de inclusão duplicada.
- Configuracao: sem variáveis usa os padrões da unidade; ignora variáveis vazias ou não relacionadas; sobrescreve apenas URL, usuário e senha.
- Persistência (PostgreSQL): gravação em cascata de caverna e setor com valores incorporados, carregamento sob demanda dos setores, violação de unicidade do código ambiental (SQLSTATE 23505), violação da restrição de profundidade (23514) e remoção de setor órfão sem apagar a caverna.

Os erros de SQL registrados no log dos testes de integração são esperados: vêm dos cenários que verificam se o banco rejeita dados inválidos. Proxies de entidade, concorrência e a interação da remoção de setores com expedições e coletas ainda não foram testados. Esta entrega não implementa a área de Alan ou Ícaro e não executa os seis casos de consulta do projeto completo.

Para repetir a verificação, na raiz do repositório:

```bash
./mvnw -B verify
docker compose --profile test up -d --wait postgres-test
./mvnw -B -Pintegracao verify
```
