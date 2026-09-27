# TurmalinaPB Expedições Científicas Subterrâneas

Projeto de Programação para a Web 3 (IFPB), desenvolvido por Victor, Alan e Ícaro. O produto é um modelo Java com Jakarta Persistence, esquema PostgreSQL, consultas e relatório técnico.

## Referência e estado atual

O [repositório setup](https://github.com/viczveras/pw3-projeto-expedicao-setup) é a referência técnica da equipe. O documento do professor, a divisão de responsabilidades e o código sanitizado estão nesse repositório, cujo acesso é privado. Esta implementação incorpora a referência gradativamente, com verificações e ajustes registrados em commits reais.

Esta etapa contém o planejamento, a configuração de build, `EntidadeBase`, `Localizacao`, `Endereco`, `Caverna`, `Setor` e seus quatro enums. Os modelos foram incorporados do setup e receberam testes de domínio. A inclusão repetida do mesmo setor foi corrigida em relação à referência.

A estrutura base também está pronta: Maven Wrapper, unidade de persistência com as entidades já incorporadas, PostgreSQL via Docker Compose, uma inicialização que valida conexão e mapeamentos, testes de integração e build no GitHub Actions. Ainda não há pessoas, console ou consultas de negócio.

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

O comando compila e executa os testes de domínio e de configuração, sem banco. Eles verificam limites de coordenadas, igualdade por valor, normalização do CEP, vínculos caverna/setor, duplicidade, inspeção e leitura das variáveis de conexão.

Testes de integração com PostgreSQL:

```bash
docker compose --profile test up -d --wait postgres-test
./mvnw -B -Pintegracao verify
```

O perfil `integracao` executa também as classes `*IT` contra o banco de testes (porta 5435, dados em memória). Cada execução cria um esquema próprio, gera as tabelas a partir dos mapeamentos e o remove ao final. Os testes cobrem persistência em cascata, carregamento sob demanda dos setores, código ambiental único, profundidade negativa rejeitada pelo banco e remoção de setor órfão. O GitHub Actions executa o mesmo comando a cada push e pull request.

## Executar

```bash
docker compose up -d --wait postgres
./mvnw -B -q compile exec:java
```

Ou inteiramente em container:

```bash
docker compose run --rm --build app
```

A saída esperada é `TurmalinaPB: conexao e mapeamentos inicializados.`, seguida da contagem de cavernas e setores. O banco de desenvolvimento escuta em `127.0.0.1:5434` e guarda os dados no volume `dados-postgres`. As tabelas são criadas ou atualizadas (`hibernate.hbm2ddl.auto=update`) sem apagar os dados existentes.

Para mudar porta ou credenciais do Compose, copie `.env.example` para `.env`. Na execução local, a conexão pode ser trocada pelas variáveis `TURMALINA_DB_URL`, `TURMALINA_DB_USER` e `TURMALINA_DB_PASSWORD`. Para encerrar o ambiente: `docker compose --profile test down`.

## Divisão de trabalho

| Integrante | Frente | Responsabilidades |
|---|---|---|
| Victor | Cadastros e infraestrutura | Base de entidades, valores incorporados, cavernas, setores, pessoas, herança, configuração e console |
| Alan | Planejamento | Expedição, plano, autorização, participação, consultas de expedição e integração do ORM |
| Ícaro | Operação e resultados | Equipamentos, movimentações, coletas, amostras, relatório final e demonstração |

Os aproximadamente 33% por integrante representam estimativa de esforço, não número igual de classes. Cada um também verifica e documenta sua área. A entrega atual inicia apenas a parte de Victor.

## Organização e desenvolvimento

- [Guia para Alan e Ícaro](docs/guia-alan-icaro.md): uso do setup, dependências, etapas e fluxo Git.
- [UML inicial dos cadastros](docs/diagrama-cadastros.md): desenho registrado antes da incorporação das classes nesta implementação.
- [Verificações das entregas de Victor](docs/validacao-victor.md): resultados e ajuste reproduzido na referência.
- `src/main/java/br/edu/ifpb/pweb3/turmalina/`: fontes incorporados durante o desenvolvimento.
- `src/main/resources/META-INF/persistence.xml`: unidade `turmalinaPU`; cada entidade entra nela no mesmo commit em que é incorporada.
- `src/test/java/br/edu/ifpb/pweb3/turmalina/`: testes dos comportamentos implementados (`*Test` sem banco, `*IT` com PostgreSQL).
- `docker-compose.yml`, `Dockerfile` e `.github/workflows/build.yml`: bancos de desenvolvimento e de testes, imagem da aplicação e build automatizado.

No repositório real, editar os fontes nesses caminhos; não criar cópias por integrante dentro da aplicação.

Cada etapa deve compilar e passar nas verificações disponíveis antes do commit. Integrações ocorrem por pull request; atualizar a branch com as dependências necessárias antes de começar uma funcionalidade que as consome.

## Entregas seguintes de Victor

1. Implementar pessoas, pesquisadores e guias com herança JOINED, liberando os contratos usados por Alan e Ícaro.
2. Revisar a remoção de setores e sua interação com as futuras expedições/coletas; a remoção herdada da referência ainda não foi revisada nesta etapa.
3. Integrar o console quando as consultas de Alan e Ícaro estiverem disponíveis.

As funcionalidades completas presentes no setup não devem ser confundidas com o que já foi incorporado aqui. As lacunas identificadas na revisão do setup precisam de testes e correções durante a implementação.
