# Verificação das entregas de Victor

Data: 27/09/2026. Referência: setup no commit `84b288071bab8af9867a789b1d681e8c9cf621c8`, pasta `organizacao-equipe/modulos/victor/codigo/`. Os fontes foram incorporados dessa referência, e não recriados do zero.

## Etapas

| Etapa | Trabalho | Verificação |
|---|---|---|
| 1 | Maven, README, guia da equipe e UML planejado | `mvn -B verify`: build aprovado; ainda sem classes/testes |
| 2 | Identidade, Endereco, Localizacao, UF e datum | `mvn -B verify`: 18 testes aprovados |
| 3 | Caverna, Setor, enums e correção de duplicidade | Teste de regressão reproduziu a falha no setup; `mvn -B verify`: 24 testes aprovados após a correção |
| 4 | Estrutura base: Maven Wrapper, `persistence.xml`, `Configuracao`, `Inicializacao`, Docker Compose, Dockerfile e CI | `./mvnw -B -Pintegracao verify`: 27 testes sem banco e 5 de integração aprovados; `docker compose run --rm --build app` e `./mvnw compile exec:java` conectaram ao PostgreSQL 17 |
| 5 | Pessoa, Pesquisador, GuiaEspeleologia, Titulacao e NivelCertificacao (herança JOINED) | `./mvnw -B verify`: 42 testes aprovados |
| 6 | Testes de integração de pessoas, base comum `IntegracaoPostgres` e correção do rollback dos testes | Falha forçada em `EntidadeBase.equals` travou a suíte antes da correção e passou a falhar em 10 s depois dela; `./mvnw -B -Pintegracao verify`: 42 + 13 testes aprovados |
| 7 | Diagrama de classes completo, registrado antes das partes de Alan e Ícaro | Atributos, tipos e valores dos enums das 16 classes conferidos por script contra o código de referência do setup |
| 8 | Recriação do esquema com script pós-criação (testes e demonstração) e configuração da demonstração | Sem `currentSchema`, o script falhou com `relation "setor" does not exist`; com o ajuste, `./mvnw -B -Pintegracao verify`: 45 + 15 testes aprovados |
| 9 | Console de consultas ao vivo (`ConsoleConsultas`, perfil Maven `console`) e busca do `public` nos testes | Sessão roteirizada de 11 comandos no banco de testes sem erro; `./mvnw -B -Pintegracao verify`: 70 + 40 testes aprovados com o banco de testes limpo e com a extensão `lo` já instalada no `public` |

## Console de consultas

O `ConsoleConsultas` veio do código de referência com dois ajustes ao projeto real, ambos encontrados ao executá-lo:

- A referência definia `jakarta.persistence.schema-generation.scripts.action=none`. Quando uma configuração de geração de esquema do JPA está presente, o Hibernate ignora `hibernate.hbm2ddl.auto`, e o `:recriar` não criava as tabelas (`relation "caverna" does not exist`). A linha foi removida: no projeto real ela não tem função, porque o `persistence.xml` não gera arquivo de esquema.
- O `:recriar` passou a usar `Configuracao.recriacaoDoEsquema`, a mesma recriação da demonstração e dos testes. Assim o banco recriado recebe o script pós-criação; na sessão de verificação, o índice `uk_autorizacao_vigente_por_expedicao` estava presente.

A sessão de verificação executou `:recriar`, `:consultas`, SQL nativo, `:mostrarsql`, as consultas nomeadas dos casos 1, 3 e 5, uma JPQL digitada e o ranking em SQL nativo, cada consulta com 1 comando SQL.

Durante essa verificação, o console e a demonstração foram executados no banco de testes, o que instalou a extensão `lo` no esquema `public`. Os testes de integração passaram a falhar com `function lo_manage() does not exist`: o `CREATE EXTENSION IF NOT EXISTS lo` do script não faz nada quando a extensão já existe, e o esquema isolado do teste não enxergava o `public`. `IntegracaoPostgres` passou a conectar com `currentSchema=<esquema do teste>,public`: as tabelas continuam resolvidas primeiro no esquema isolado, e as funções da extensão são encontradas onde estiverem.

## Script pós-criação nos testes de integração

O `pos-criacao.sql` do setup (arquivo de Alan) cria regras que o Hibernate não gera, como o índice único parcial de autorização vigente. Ele usa nomes de tabela sem esquema. Nos testes, as tabelas ficam num esquema isolado (`hibernate.default_schema`), mas essa propriedade só qualifica o DDL do Hibernate, não os comandos do script: o script procurava as tabelas no esquema `public` e falhava.

`IntegracaoPostgres` passou a conectar com `currentSchema` igual ao esquema do teste, e `Configuracao.recriacaoDoEsquema` inclui o script quando o arquivo existe no classpath. A verificação usa um script apenas de teste (`src/test/resources/sql/teste-pos-criacao.sql`), com um índice único parcial em `setor`, em `ScriptPosCriacaoIT`. Com `currentSchema` removido de propósito, a criação falhou com `relation "setor" does not exist`; com ele, o índice aparece no esquema do teste e o banco rejeita a violação (23505). O script real de Alan passa a ser executado automaticamente quando for incorporado, sem mudança nos arquivos de Victor.

## Ajuste real encontrado nos testes de integração

Para confirmar que o teste de igualdade entre proxy e subtipo detecta um erro de verdade, `EntidadeBase.equals` foi alterado temporariamente para comparar `getClass()`. O teste falhou como esperado: o proxy devolvido por `getReference(Pessoa.class, id)` é da classe `Pessoa$HibernateProxy`, diferente de `Pesquisador`. Porém a suíte não terminou: ficou parada por mais de 5 minutos.

Causa: o auxiliar `transacao` só desfazia a transação em `RuntimeException`. Uma asserção que falha lança `AssertionError`, então a transação ficava aberta, segurando bloqueios na tabela `pessoa`, e a remoção das tabelas ao final (`create-drop`) esperava indefinidamente. Qualquer teste de integração que falhasse travaria o build local e o CI em vez de relatar a falha.

O ajuste move o rollback para um bloco `finally`, executado para qualquer erro. Com a mesma alteração forçada, a suíte passou a falhar em 10 segundos, apontando o teste. `EntidadeBase` foi restaurada e a suíte completa foi executada novamente.

## Ajuste real encontrado na referência

Sequência: criar uma caverna e um setor, adicionar o setor e tentar adicionar novamente a mesma instância. O setup chama `setores.add` novamente e passa a apresentar duas entradas para o mesmo objeto.

O teste `CavernaTest.rejeitaAdicionarMesmoSetorDuasVezes` foi executado antes da correção com `mvn -B -Dtest=CavernaTest test`. O resultado foi 6 testes, 1 falha: era esperada `IllegalStateException`, mas nenhuma exceção foi lançada. Não houve inserção proposital de defeito; o comportamento já existia no código de referência.

O ajuste rejeita a repetição antes de modificar o vínculo. A comparação usa a identidade da entidade, conforme o contrato já fornecido por `EntidadeBase.mesmaEntidade`. O teste exige que, após a tentativa rejeitada, a coleção continue com um único setor e o vínculo original permaneça intacto. Entidades distintas com a mesma denominação continuam sujeitas à restrição relacional do setup; esta correção trata a repetição da mesma entidade na coleção.

## Escopo verificado

- Localizacao: quatro extremos válidos, quatro coordenadas fora do limite, igualdade com escalas decimais diferentes, datum e campos obrigatórios.
- Endereco: CEP normalizado com zero inicial, tamanho inválido, ausência de CEP e igualdade/diferença de complementos.
- Caverna/Setor: vínculo dos dois lados, rejeição de transferência, proteção da coleção, inspeção, identidade de setores transientes e rejeição de inclusão duplicada.
- Pessoa/Pesquisador/GuiaEspeleologia: CPF normalizado e com 11 dígitos, e-mail sem espaços e em minúsculas, campos obrigatórios, situação ativa, validade da certificação inclusive no último dia, renovação e contagem de expedições concluídas.
- Configuracao: sem variáveis usa os padrões da unidade; ignora variáveis vazias ou não relacionadas; sobrescreve apenas URL, usuário e senha; recriação do esquema inclui o script só quando ele existe; demonstração recria o esquema e ativa SQL, formatação e estatísticas.
- Persistência (PostgreSQL): gravação em cascata de caverna e setor com valores incorporados, carregamento sob demanda dos setores, violação de unicidade do código ambiental (SQLSTATE 23505), violação da restrição de profundidade (23514) e remoção de setor órfão sem apagar a caverna.
- Pessoas (PostgreSQL): cada tipo gravado na própria tabela com `tipo_pessoa` (PESSOA, PESQUISADOR, GUIA) e endereço na tabela `pessoa`; `find(Pessoa.class, id)` e `type(p)` devolvem o subtipo concreto; proxy da raiz igual ao subtipo carregado; unicidade de CPF (mesmo com formatação diferente), e-mail (mesmo com maiúsculas) e registro institucional (23505); bolsa negativa rejeitada pela restrição da tabela `pesquisador` (23514); colunas `boolean`, `date` e `numeric` nativas.

Os erros de SQL registrados no log dos testes de integração são esperados: vêm dos cenários que verificam se o banco rejeita dados inválidos. Concorrência e a interação da remoção de setores com expedições e coletas ainda não foram testadas. O CPF é validado apenas pela quantidade de dígitos, sem dígitos verificadores, como na referência. Esta entrega não implementa a área de Alan ou Ícaro e não executa os seis casos de consulta do projeto completo.

Para repetir a verificação, na raiz do repositório:

```bash
./mvnw -B verify
docker compose --profile test up -d --wait postgres-test
./mvnw -B -Pintegracao verify
```
