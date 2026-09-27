# Verificação da primeira entrega de Victor

Data: 27/09/2026. Referência: setup no commit `84b288071bab8af9867a789b1d681e8c9cf621c8`, pasta `organizacao-equipe/modulos/victor/codigo/`. Os fontes foram incorporados dessa referência, e não recriados do zero.

## Etapas

| Etapa | Trabalho | Verificação |
|---|---|---|
| 1 | Maven, README, guia da equipe e UML planejado | `mvn -B verify`: build aprovado; ainda sem classes/testes |
| 2 | Identidade, Endereco, Localizacao, UF e datum | `mvn -B verify`: 18 testes aprovados |
| 3 | Caverna, Setor, enums e correção de duplicidade | Teste de regressão reproduziu a falha no setup; `mvn -B verify`: 24 testes aprovados após a correção |

## Ajuste real encontrado na referência

Sequência: criar uma caverna e um setor, adicionar o setor e tentar adicionar novamente a mesma instância. O setup chama `setores.add` novamente e passa a apresentar duas entradas para o mesmo objeto.

O teste `CavernaTest.rejeitaAdicionarMesmoSetorDuasVezes` foi executado antes da correção com `mvn -B -Dtest=CavernaTest test`. O resultado foi 6 testes, 1 falha: era esperada `IllegalStateException`, mas nenhuma exceção foi lançada. Não houve inserção proposital de defeito; o comportamento já existia no código de referência.

O ajuste rejeita a repetição antes de modificar o vínculo. A comparação usa a identidade da entidade, conforme o contrato já fornecido por `EntidadeBase.mesmaEntidade`. O teste exige que, após a tentativa rejeitada, a coleção continue com um único setor e o vínculo original permaneça intacto. Entidades distintas com a mesma denominação continuam sujeitas à restrição relacional do setup; esta correção trata a repetição da mesma entidade na coleção.

## Escopo verificado

- Localizacao: quatro extremos válidos, quatro coordenadas fora do limite, igualdade com escalas decimais diferentes, datum e campos obrigatórios.
- Endereco: CEP normalizado com zero inicial, tamanho inválido, ausência de CEP e igualdade/diferença de complementos.
- Caverna/Setor: vínculo dos dois lados, rejeição de transferência, proteção da coleção, inspeção, identidade de setores transientes e rejeição de inclusão duplicada.

Os testes são de domínio, sem banco. Persistência, esquema PostgreSQL, proxies reais, concorrência, cascatas em banco e remoção de setores ainda precisam de testes de integração numa próxima etapa. Esta entrega não implementa a área de Alan ou Ícaro e não executa os seis casos de consulta do projeto completo.

Para repetir a verificação, na raiz do repositório:

```bash
mvn -B verify
```
