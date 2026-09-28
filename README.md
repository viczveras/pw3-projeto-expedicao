# TurmalinaPB Expedições Científicas Subterrâneas

Projeto de Programação para a Web 3 (IFPB), desenvolvido por Victor, Alan e Ícaro. O produto é um modelo Java com Jakarta Persistence, esquema PostgreSQL, consultas e relatório técnico.

## Referência e estado atual

O [repositório setup](https://github.com/viczveras/pw3-projeto-expedicao-setup) é a referência técnica da equipe. O documento do professor, a divisão de responsabilidades e o código sanitizado estão nesse repositório, cujo acesso é privado. Esta implementação incorpora a referência gradativamente, com verificações e ajustes registrados em commits reais.

A parte de cadastros está incorporada: `EntidadeBase`, `Localizacao`, `Endereco`, `Caverna`, `Setor`, `Pessoa`, `Pesquisador`, `GuiaEspeleologia` e seus seis enums. Pessoas usam herança JOINED, com o endereço incorporado na tabela `pessoa`. Os modelos vieram do setup e receberam testes de domínio e de integração. A inclusão repetida do mesmo setor foi corrigida em relação à referência.

A estrutura base também está pronta: Maven Wrapper, unidade de persistência com as entidades já incorporadas, PostgreSQL via Docker Compose, uma inicialização que valida conexão e mapeamentos, testes de integração e build no GitHub Actions. Ainda não há expedições, equipamentos, coletas, console ou consultas de negócio.

## Ambiente

- JDK 21.
- Maven 3.9.9, baixado automaticamente pelo Maven Wrapper (`mvnw`/`mvnw.cmd`); não é preciso instalá-lo.
- Docker com Compose, para o PostgreSQL 17 e a execução em container.
- Hibernate 6.6.4.Final e driver PostgreSQL 42.7.4, mantendo as versões do setup.
- JUnit Jupiter 5.11.4 para os testes adicionados nesta implementação.

## Compilar e testar

Na raiz deste repositório (no PowerShell, use `.\mvnw.cmd` no lugar de `./mvnw`):

```bash
./mvnw -B verify
```

O comando compila e executa os testes de domínio e de configuração, sem banco. Eles verificam limites de coordenadas, igualdade por valor, normalização do CEP e do CPF, vínculos caverna/setor, duplicidade, inspeção, especializações de pessoa, validade da certificação do guia e leitura das variáveis de conexão.

Testes de integração com PostgreSQL:

```bash
docker compose --profile test up -d --wait postgres-test
./mvnw -B -Pintegracao verify
```

O perfil `integracao` executa também as classes `*IT` contra o banco de testes (porta 5435, dados em memória). Cada classe herda de `IntegracaoPostgres`, que cria um esquema próprio, gera as tabelas a partir dos mapeamentos e o remove ao final. Os testes cobrem:

- cavernas: persistência em cascata, carregamento sob demanda dos setores, código ambiental único, profundidade negativa rejeitada e remoção de setor órfão;
- pessoas: tabelas da herança JOINED com discriminador, consulta polimórfica, igualdade entre proxy e subtipo, CPF, e-mail e registro únicos, bolsa negativa rejeitada e tipos nativos do PostgreSQL (`boolean`, `date`, `numeric`);
- script pós-criação: executado no esquema do teste depois das tabelas, com índice único parcial aplicado pelo banco.

Quando existir `src/main/resources/META-INF/sql/pos-criacao.sql`, a base dos testes o executa automaticamente depois de criar as tabelas, com o esquema do teste como `search_path`. Assim, regras que só o banco garante (como o índice parcial de autorização vigente) também são testadas. O GitHub Actions executa o mesmo comando a cada push e pull request.

## Executar

```bash
docker compose up -d --wait postgres
./mvnw -B -q compile exec:java
```

Ou inteiramente em container:

```bash
docker compose run --rm --build app
```

A saída esperada é `TurmalinaPB: conexao e mapeamentos inicializados.`, seguida da contagem de cavernas, setores e pessoas. O banco de desenvolvimento escuta em `127.0.0.1:5434` e guarda os dados no volume `dados-postgres`. As tabelas são criadas ou atualizadas (`hibernate.hbm2ddl.auto=update`) sem apagar os dados existentes. Nesse modo o script pós-criação não é executado.

A demonstração usa outra configuração, `Configuracao.propriedadesDaDemonstracao()`: recria o esquema (apaga os dados do banco configurado), executa o script pós-criação quando ele existir e exibe o SQL gerado e as estatísticas do Hibernate, que servem de evidência contra N+1. Execute-a só em banco descartável.

Para mudar porta ou credenciais do Compose, copie `.env.example` para `.env`. Na execução local, a conexão pode ser trocada pelas variáveis `TURMALINA_DB_URL`, `TURMALINA_DB_USER` e `TURMALINA_DB_PASSWORD`. Para encerrar o ambiente: `docker compose --profile test down`.

## Divisão de trabalho

| Integrante | Frente | Responsabilidades |
|---|---|---|
| Victor | Cadastros e infraestrutura | Base de entidades, valores incorporados, cavernas, setores, pessoas, herança, configuração e console |
| Alan | Planejamento | Expedição, plano, autorização, participação, consultas de expedição e integração do ORM |
| Ícaro | Operação e resultados | Equipamentos, movimentações, coletas, amostras, relatório final e demonstração |

Os aproximadamente 33% por integrante representam estimativa de esforço, não número igual de classes. Cada um também verifica e documenta sua área. Até aqui, só a parte de Victor foi incorporada.

## Organização e desenvolvimento

- [Diagrama de classes completo](docs/diagrama-classes.md): as 14 entidades, os 2 tipos incorporáveis, associações, cardinalidades, enums e unicidades.
- [UML inicial dos cadastros](docs/diagrama-cadastros.md): desenho registrado antes da incorporação das classes de Victor.
- [Verificações das entregas de Victor](docs/validacao-victor.md): resultados e ajuste reproduzido na referência.
- `src/main/java/br/edu/ifpb/pweb3/turmalina/`: fontes incorporados durante o desenvolvimento.
- `src/main/resources/META-INF/persistence.xml`: unidade `turmalinaPU`; cada entidade entra nela no mesmo commit em que é incorporada.
- `src/test/java/br/edu/ifpb/pweb3/turmalina/`: testes dos comportamentos implementados (`*Test` sem banco, `*IT` com PostgreSQL, uma classe `*IT` por área).
- `docker-compose.yml`, `Dockerfile` e `.github/workflows/build.yml`: bancos de desenvolvimento e de testes, imagem da aplicação e build automatizado.

No repositório real, editar os fontes nesses caminhos; não criar cópias por integrante dentro da aplicação.

Cada etapa deve compilar e passar nas verificações disponíveis antes do commit. Integrações ocorrem por pull request; atualizar a branch com as dependências necessárias antes de começar uma funcionalidade que as consome.

## Entregas seguintes de Victor

1. Revisar a remoção de setores e sua interação com as futuras expedições/coletas; a remoção herdada da referência ainda não foi revisada nesta etapa.
2. Integrar o console quando as consultas de Alan e Ícaro estiverem disponíveis.
3. Atualizar o README final da entrega.

As funcionalidades completas presentes no setup não devem ser confundidas com o que já foi incorporado aqui. As lacunas identificadas na revisão do setup precisam de testes e correções durante a implementação.
