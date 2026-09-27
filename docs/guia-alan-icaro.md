# Guia de continuidade — Alan e Ícaro

## Repositórios e referência

- Desenvolvimento: https://github.com/viczveras/pw3-projeto-expedicao
- Referência técnica privada: https://github.com/viczveras/pw3-projeto-expedicao-setup
- Snapshot de referência desta primeira entrega: commit `84b288071bab8af9867a789b1d681e8c9cf621c8` do setup.
- Código de Alan: `organizacao-equipe/modulos/alan/codigo/` no setup.
- Código de Ícaro: `organizacao-equipe/modulos/icaro/codigo/` no setup.
- Código completo para consultar dependências: `organizacao-equipe/projeto-sanitizado/` no setup.
- Roteiros, listas de arquivos e lacunas: `organizacao-equipe/modulos/<nome>/ROTEIRO.md`, `arquivos.json` e `01-analise-de-cobertura.md`.

É necessário ter acesso ao setup para clonar a referência. O documento do professor define o escopo acadêmico; o setup é a fonte técnica acordada para nomes, contratos, mapeamentos e organização. Há falhas conhecidas na referência, que devem ser reproduzidas e corrigidas de forma explícita.

## O que a parte de Victor já fornece

Na `main`: build, `EntidadeBase`, `Endereco`, `Localizacao`, `Caverna`, `Setor`, `Pessoa`, `Pesquisador`, `GuiaEspeleologia` e seus enums, com testes de domínio e de integração. Também a estrutura base: Maven Wrapper, `persistence.xml`, PostgreSQL via Docker Compose, a base `IntegracaoPostgres` para testes com banco e CI. Os construtores e nomes de colunas são os mesmos do setup, então as classes de vocês que referenciam pessoas, pesquisadores, cavernas e setores compilam sem adaptação. Console e demonstração completa são posteriores.

O cadastro já rejeita adicionar novamente o mesmo setor à mesma caverna, correção reproduzida e documentada em [validação de Victor](validacao-victor.md). Portanto, não substituam indiscriminadamente classes integradas pela versão mais antiga do setup. Divergências intencionais devem ser explicadas e testadas.

## Preparar o ambiente e as branches

Cada integrante usa sua identidade verdadeira no Git e um clone próprio. Os comandos abaixo pressupõem que a branch ainda não existe localmente.

```bash
git clone https://github.com/viczveras/pw3-projeto-expedicao.git
cd pw3-projeto-expedicao
git fetch origin
git switch -c feat/alan-planejamento origin/main
./mvnw -B verify
```

No PowerShell, use `.\mvnw.cmd`. Não é preciso instalar o Maven; basta o JDK 21 e, para os testes com banco, o Docker.

Ícaro troca apenas o comando de criação da branch:

```bash
git switch -c feat/icaro-operacao origin/main
```

Toda branch nova parte de `origin/main`. Se alguém já tiver uma branch própria, não a recrie nem sobrescreva: incorpore a `main` com merge e confira o diff.

Os patches numerados do setup removem comentários do projeto antigo; não adicionam as funcionalidades que faltam neste repositório. Para iniciar aqui, use os arquivos reais em `codigo/` como referência, preservando o pacote `br.edu.ifpb.pweb3.turmalina` e os caminhos `src/main/java` e `src/main/resources`.

## Alan — sequência proposta

1. Conferir no PDF os requisitos de expedição, plano, autorização e participação; desenhar seu recorte UML e listar os contratos com Victor/Ícaro.
2. Incorporar seus enums e definir testes/cenários. O agregado `Expedicao` depende de `Pessoa` e `Setor` (Victor, já na `main`), `Coleta` e `RelatorioFinal` (Ícaro).
3. Incorporar o núcleo acoplado junto com Ícaro, na branch compartilhada descrita abaixo. Não publicar referências a classes inexistentes como se o build estivesse pronto.
4. Verificar participantes, plano obrigatório/exclusivo, autorização vigente única e transições. Reproduzir a troca de plano rejeitada que desfaz o vínculo antigo e a remoção de setor usado em coleta antes de corrigi-las.
5. Implementar projeções de expedição/participantes e downloads. Consolidar `orm.xml` e o SQL de criação somente quando houver entidades correspondentes. A limpeza global de large objects do setup precisa ser revista.
6. Testar as regras no domínio e depois no PostgreSQL; atualizar o relatório técnico com a decisão efetivamente implementada.

Commits possíveis: `feat(planejamento): adicionar estados da expedicao`; `feat(planejamento): integrar agregado e participantes`; `fix(planejamento): preservar vinculos apos troca rejeitada`. Criar cada commit quando a mudança correspondente existir e estiver verificada.

## Ícaro — sequência proposta

1. Conferir equipamentos, movimentações, coletas, amostras e relatório no PDF; desenhar seu recorte UML e conferir os vínculos com Alan/Victor.
2. Começar por `Equipamento`, `TipoEquipamento`, `SituacaoOperacional` e os testes de invariantes. Esse conjunto depende da `EntidadeBase` comum e pode avançar antes de `Expedicao`.
3. Preparar os demais enums e incorporar `Coleta`, `Amostra`, `RelatorioFinal` e `MovimentacaoEquipamento` junto com Alan, na branch compartilhada descrita abaixo. `Pessoa` e `Pesquisador` já estão na `main`.
4. Incorporar amostras e movimentações quando as dependências estiverem disponíveis. Testar quantidade, datas e devolução; confirmar a decisão sobre equipamento aguardando calibração antes de alterar a regra do setup.
5. Implementar consultas de disponibilidade, coletas/amostras e indicadores; enviar a Alan os trechos de ORM e os cenários esperados para integração.
6. Incorporar a massa de exemplo e a demonstração depois da integração dos três módulos, e coletar evidências SQL dos seis casos exigidos.

Commits possíveis: `feat(equipamentos): adicionar cadastro patrimonial`; `test(equipamentos): verificar regras de movimentacao`; `feat(coletas): incorporar amostras e consultas`. Indicadores adicionais são extras; priorizar os seis casos obrigatórios.

## Núcleo acoplado: Alan e Ícaro juntos

`Expedicao` referencia `Coleta` e `RelatorioFinal`; `Coleta`, `RelatorioFinal` e `MovimentacaoEquipamento` referenciam `Expedicao`. Nenhum dos dois lados compila sozinho. Por isso essas classes entram numa branch única, de preferência numa sessão em que os dois trabalham juntos:

```bash
git switch -c feat/nucleo-expedicao origin/main
```

- Alan faz os commits de `Expedicao`, `PlanoSeguranca`, `AutorizacaoAmbiental`, `Participacao` e seus enums.
- Ícaro faz os commits de `Coleta`, `Amostra`, `RelatorioFinal`, `MovimentacaoEquipamento` e seus enums.
- Cada um faz `git pull` antes de começar e `git push` logo após cada commit, para o outro receber as classes de que depende.
- O PR para `main` só é aberto quando `./mvnw -B -Pintegracao verify` passar com o núcleo completo.

`Equipamento` e as classes que não dependem do núcleo podem continuar nas branches individuais.

## Contratos e integração

| Arquivos/contrato | Responsável | Como coordenar |
|---|---|---|
| POM, configuração, EntidadeBase, Pessoa, Pesquisador, Caverna e Setor | Victor | Combinar alterações de assinatura e novas dependências |
| Expedicao, participantes, segurança e `orm.xml` | Alan | Receber queries de Ícaro e integrar no arquivo único |
| Coleta, Amostra, RelatorioFinal, equipamentos e demonstração | Ícaro | Integrar com os pontos de vínculo chamados por Expedicao |
| Documentação de cada área | Autor da área | Atualizar o texto na mesma mudança da implementação |

Cada pessoa deve incorporar apenas os arquivos da sua responsabilidade e os testes correspondentes. Não copiar `pom.xml`, `persistence.xml` ou `orm.xml` completos do setup sobre versões já alteradas pelos colegas. O `persistence.xml` deste repositório lista apenas as entidades já incorporadas e atualiza o esquema sem apagá-lo; o do setup lista todas e recria o esquema. Ao incorporar uma entidade, acrescentem a linha `<class>` correspondente no mesmo commit e validem os mapeamentos com os testes de integração.

Testes com banco: criem uma classe `*IT` própria por área (por exemplo `ExpedicaoIT`, `EquipamentoIT`, `ColetaIT`) que herde de `IntegracaoPostgres`, em `src/test/java/br/edu/ifpb/pweb3/turmalina/app/`. A base cria e remove um esquema isolado e oferece `transacao(em -> ...)`, `exigirSqlState(erro, "23505")` e `tipoDaColuna(tabela, coluna)`. Usem `CavernaIT` e `PessoaIT` como exemplos. Não editem as classes `*IT` dos colegas: isso evita conflitos no merge.

Antes de cada commit:

```bash
git status --short
git diff
./mvnw -B verify
docker compose --profile test up -d --wait postgres-test
./mvnw -B -Pintegracao verify
git add -- caminho/do/arquivo1 caminho/do/arquivo2
git diff --cached --check
git commit -m "mensagem que descreve a mudanca real"
git push -u origin NOME_DA_SUA_BRANCH
```

Substituir os caminhos e o nome da branch pelos próprios arquivos. O merge não garante compatibilidade de métodos nem valida regras de negócio; compilar e executar os cenários afetados na integração. Não esperar todas as funcionalidades ficarem prontas para integrar as primeiras entregas.

Já existe a proposta de README [PR #1](https://github.com/viczveras/pw3-projeto-expedicao/pull/1), na branch `copilot/create-readme`. Esta entrega não altera essa proposta. Ao integrar os READMEs, preservar tanto a descrição dos requisitos quanto o estado real da implementação; não declarar como prontas as funcionalidades ainda ausentes.

## Para a apresentação

Cada integrante deve explicar seus atributos, tipos, cardinalidades, proprietário das associações e estratégia de carregamento; mostrar um caso válido e um inválido; e relacionar a implementação ao PDF. O histórico registra a incorporação e os ajustes reais da referência, com as datas reais de trabalho. Não é necessário introduzir falhas intencionais para demonstrar desenvolvimento incremental.

Referência dos testes: [guia oficial do JUnit 5.11.4](https://docs.junit.org/5.11.4/user-guide/).
