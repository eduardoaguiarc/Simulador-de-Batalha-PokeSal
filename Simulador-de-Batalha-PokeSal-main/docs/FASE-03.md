# Fase 03 — implementação e validação

> **Suíte reduzida:** consulte a [suíte mínima atual](testes/SUITE-MINIMA.md). As contagens, resultados e coberturas abaixo descrevem a execução histórica de 81 testes, anterior à suíte atual de 19 testes. Referências a testes removidos são históricas.

> Atualização das etapas 4.2–4.6:
> - Requisitos autorais, suíte Mockito e JaCoCo: [Requisitos autorais, Mockito e JaCoCo](testes/FASE-03-MOCKITO-JACOCO.md) (81 testes aprovados, 95,90% de linhas e 85,71% de branches no escopo de negócio).
> - SonarQube comparativo (Fase 02 vs. Fase 03) e CI: [Relatório Comparativo SonarQube](sonarqube/RELATORIO.md) (`Coverage: 36,4% → 78,1%`, `Duplications: 1,5% → 1,1%`, Quality Gate **OK**).
> - Rastreabilidade, justificativa de mocks e registro de bugs: [Matriz de Rastreabilidade](testes/MATRIZ-RASTREABILIDADE.md) e [Documento de Testes e Registro de Bugs](testes/RELATORIO-TESTES.md).
> A execução de 48 testes descrita abaixo permanece como histórico da etapa inicial.

As regras de tipos elementais, dano-base mínimo, HP, status, quatro golpes por
Pokésal, usos limitados e dois itens por treinador por batalha foram preservadas.
Os artefatos anteriores permanecem como histórico; este documento registra a evolução.

## Regras implementadas

### Precisão e crítico

- `Golpe.precisao` aceita valores finitos de 0 a 100. A configuração dos golpes
  iniciais passou de 1,0/0,9/0,85/0,75 para 100/90/85/75.
- Acerta quando `GeradorAleatorio.proximoDouble() < precisao / 100`.
- A tentativa consome um uso mesmo se errar. Um erro gera evento de dano zero,
  sem sortear crítico ou aplicar status.
- Depois do acerto, um novo sorteio abaixo de 0,0625 indica crítico.
- O cálculo é `round(max(1, ATK efetivo + poder - DEF) × tipo × terreno × crítico)`;
  crítico vale 1,5, e o arredondamento acontece apenas no final.
- Acerto, crítico, aplicação de status e desempates usam a mesma interface injetada.
  A única instanciação de `Random` está em `Main`, que monta a aplicação.

### Terreno e relógio

`Arena` recebe `ServicoTerreno` pelo construtor. `ServicoTerrenoClimatico` recebe
um `Clock` e um `Supplier<Clima>` pelo construtor e consulta ambos a cada efeito.
Nenhuma classe de regra cria um relógio de sistema.

Como o enunciado só exemplifica a ativação diurna do asfalto, foram adotadas estas
condições explícitas:

| Terreno | Condição | Efeito |
| --- | --- | --- |
| Asfalto Quente | Ensolarado e hora local entre 06:00 inclusive e 18:00 exclusive | Fogo ×1,15 |
| Poça de Chuva | Chuvoso, em qualquer horário | Água ×1,10 |
| Canteiro Central | Qualquer clima/horário | Planta recupera 5% do HP máximo |

O console usa `America/Bahia` e clima informado pelo jogador. Outras fontes de
clima podem ser fornecidas sem alterar a batalha. A cura não ultrapassa o máximo
nem revive um Pokésal derrotado.

### Torneio, resultados e ranking

- Somente 4 ou 8 participantes, com identificadores distintos e objetos próprios
  de Pokésal e mochila. A ordem de inscrição define as chaves: 1×2, 3×4 etc.
- São disputadas 3 ou 7 batalhas, respectivamente. Cada vencedor avança.
- Antes de cada batalha do torneio, HP, status e usos dos golpes são restaurados.
  A mochila mantém os itens restantes; o limite de dois usos reinicia por batalha.
- O ranking do torneio atual ordena por vitórias decrescentes. Empatados recebem a
  mesma posição; a exibição mantém a ordem de inscrição. Não há disputa de terceiro lugar.
- `ResultadoBatalha` captura vencedor, perdedor, turnos e identidades dos dois
  Pokésal, sem referências ao estado mutável que será recuperado na rodada seguinte.
- A batalha tenta salvar seu resultado uma única vez. Uma exceção do repositório
  gera `ERRO_REPOSITORIO`, com ID da batalha, turno, classe e mensagem do erro.
  O resultado continua válido em memória e entra no ranking mesmo se não foi salvo.
- O adaptador CSV acrescenta registros UTF-8 com cabeçalho e escape de campos,
  sem apagar resultados anteriores. O arquivo fica no diretório de execução.
  O ID da batalha é local à execução; não é um identificador global entre torneios.
- Reexecutar a mesma instância de batalha ou torneio é rejeitado.

Dois casos sem regra explícita no enunciado receberam uma política de encerramento:
derrota simultânea por status é decidida por sorteio injetado; tentar atacar com
todos os golpes esgotados resulta em derrota por falta de golpes, evitando um
laço infinito de seleção. O uso de itens disponíveis continua possível antes disso.

### Registro e inversão de dependência

Cada turno acumula eventos imutáveis e os envia ao `RegistroBatalha` nesta ordem:
`ATAQUE → DANO → EFEITO_STATUS → EFEITO_TERRENO → USO_ITEM`.
A ordenação é estável dentro de cada categoria. Apenas eventos que ocorreram
são enviados, incluindo dano zero em erros, status aplicado/dano periódico,
modificadores/cura de terreno e consumo de item. Eventos incluem batalha e turno.

Essa é a ordem de **envio ao registro**. As ações continuam seguindo velocidade,
e usar um item continua ocupando a ação do treinador. A mecânica anterior de
fim de turno (cura do terreno antes do dano de status de cada Pokésal) foi mantida.
O registro fica ordenado mesmo quando o treinador mais rápido usa um item.
Métodos de ação chamados isoladamente, fora de `executarTurno`, enviam seus eventos
imediatamente; o agrupamento por turno pertence a `executarTurno`.

`Main` monta relógio, gerador, serviço, repositório, registro e fábrica. O torneio
recebe `FabricaBatalha`, a batalha recebe gerador/repositório/registro/arena/leitor,
e a arena recebe o serviço. Não há instanciação desses colaboradores nas regras.

## Rastreabilidade e testes

| Requisito | Implementação | Evidência automatizada |
| --- | --- | --- |
| 2.1 Precisão e dano zero | `Golpe`, `Batalha.executarAtaque` | `Fase03BatalhaTest`: 0%, 100%, limite de 75%, precisão inválida, uso consumido |
| 2.1 Crítico 6,25% ×1,5 | `Batalha` | Sorteios 0, 0,062499999 e 0,0625; combinação com tipo/terreno |
| 2.1 Aleatoriedade injetada | `GeradorAleatorio` | Sequências controladas de acerto, crítico, status e desempates |
| 2.2 Clima e horário | `ServicoTerrenoClimatico`, `Arena` | `ServicoTerrenoTest`: fronteiras 06h/18h, fuso, mudança de clima, cura limitada |
| 2.3 Eliminatórias e ranking | `Torneio` | `TorneioTest`: chaves completas de 4/8, restauração, vitórias/derrotas, participantes inválidos |
| 2.3 Persistência tolerante a falha | `Batalha`, `RepositorioResultados` | Falha em todas as 3/7 gravações, erro registrado e campeão/ranking preservados |
| 2.3 Dados persistidos | `ResultadoBatalha`, `RepositorioResultadosCsv` | Identidades e turnos; arquivo real temporário, append, UTF-8, vírgulas e aspas |
| 2.4 Ordem do registro | `EventoBatalha`, `RegistroBatalha` | Turno com todas as categorias, item como primeira ação, dois ataques, nocaute |
| 2.5 Construtores | Interfaces e `Main` | Fakes injetados nos testes e inspeção das fontes de tempo/aleatoriedade |
| Regressão das fases anteriores | Tipos, status, HP, golpes, itens | Testes existentes adaptados; cura antes do veneno e encerramento por nocaute |

Em 02/11/2026, a compilação com JDK 25.0.4.1, `--release 21`, e a execução pelo
JUnit Platform Launcher 1.14.0/Jupiter 5.14.0 resultaram em **48 testes aprovados,
zero falhas e zero testes ignorados**. O antigo teste vazio de usos foi preenchido
com verificação de consumo e restauração.

[Resumo produzido pelo executor JUnit](testes/evidencias/fase-03-junit.txt).

Também foi executado `Main` como processo Java com entradas pelo console nos três
modos: batalha avulsa (1 resultado), torneio de 4 (3 resultados) e torneio de 8
(7 resultados). Todos encerraram com sucesso; os torneios exibiram campeão e
ranking, e os CSVs tiveram os registros esperados.

O `pom.xml` permite reproduzir a suíte com `mvn test` dentro da pasta do projeto.
Nesta execução inicial de 48 testes, os testes foram compilados com `javac` e executados pelo launcher
com as bibliotecas JUnit locais. A execução posterior com Maven, JaCoCo e SonarQube (81 testes) está documentada nos relatórios das etapas 4.2–4.6.
