# TurmalinaPB — Expedições Científicas Subterrâneas

Projeto de Programação para a Web 3 (IFPB), desenvolvido por Victor, Alan e Ícaro. O produto é um modelo Java com Jakarta Persistence, esquema PostgreSQL, consultas e relatório técnico.

## Referência e estado atual

O [repositório setup](https://github.com/viczveras/pw3-projeto-expedicao-setup) é a referência técnica da equipe. O documento do professor, a divisão de responsabilidades e o código sanitizado estão nesse repositório, cujo acesso é privado. Esta implementação incorpora a referência gradativamente, com verificações e ajustes registrados em commits reais.

Esta primeira etapa contém o planejamento e a configuração de build. Os modelos serão incorporados nas próximas entregas da branch `feat/victor-cadastros`. Ainda não há aplicação executável, unidade de persistência ou consultas de negócio.

## Ambiente

- JDK 21.
- Maven 3.9 ou superior.
- Hibernate 6.6.4.Final e driver PostgreSQL 42.7.4, mantendo as versões do setup.
- JUnit Jupiter 5.11.4 para os testes adicionados nesta implementação.

Na raiz deste repositório:

```bash
mvn -B verify
```

Nesta etapa inicial, o comando valida o build sem executar testes de domínio, pois eles ainda serão implementados. Banco de dados não é necessário para compilar. A configuração e os testes de integração com PostgreSQL serão adicionados numa etapa posterior.

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
- `src/main/java/br/edu/ifpb/pweb3/turmalina/`: fontes incorporados durante o desenvolvimento.
- `src/test/java/br/edu/ifpb/pweb3/turmalina/`: testes dos comportamentos implementados.

As pastas `src/` passam a existir quando suas primeiras classes forem adicionadas. No repositório real, editar os fontes nesses caminhos; não criar cópias por integrante dentro da aplicação.

Cada etapa deve compilar e passar nas verificações disponíveis antes do commit. Integrações ocorrem por pull request; atualizar a branch com as dependências necessárias antes de começar uma funcionalidade que as consome.

## Entregas seguintes de Victor

1. Incorporar a identidade das entidades e os valores `Localizacao` e `Endereco`, com testes.
2. Incorporar `Caverna` e `Setor`, verificando os vínculos e as invariantes.
3. Implementar pessoas, pesquisadores e guias com herança JOINED.
4. Configurar persistência, testar os mapeamentos no PostgreSQL e integrar o console quando as consultas estiverem disponíveis.

As funcionalidades completas presentes no setup não devem ser confundidas com o que já foi incorporado aqui. As lacunas identificadas na revisão do setup precisam de testes e correções durante a implementação.
