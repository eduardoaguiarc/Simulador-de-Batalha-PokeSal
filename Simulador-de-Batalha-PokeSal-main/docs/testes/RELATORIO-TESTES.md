# Documento de Testes, Cobertura e Registro de Bugs — Fase 03

> **Suíte reduzida:** consulte a [suíte mínima atual](SUITE-MINIMA.md). As contagens, resultados e coberturas abaixo descrevem a execução histórica de 81 testes, anterior à suíte atual de 19 testes. Referências a testes removidos são históricas.

Este documento consolida a evolução da suíte de testes do **PokéSal** entre a Fase 02 e a Fase 03, apresenta o registro detalhado dos bugs revelados pelos novos testes (com as respectivas correções e commits) e preserva o catálogo histórico dos testes originais da Fase 02.

- **Matriz de rastreabilidade completa e justificativa dos mocks:** [`MATRIZ-RASTREABILIDADE.md`](MATRIZ-RASTREABILIDADE.md)
- **Especificação dos requisitos autorais, suíte Mockito e configuração JaCoCo:** [`FASE-03-MOCKITO-JACOCO.md`](FASE-03-MOCKITO-JACOCO.md)
- **Relatório comparativo SonarQube e Integração Contínua:** [`../sonarqube/RELATORIO.md`](../sonarqube/RELATORIO.md)

---

## 1. Visão Geral da Suíte na Fase 03

Na Fase 03, a suíte evoluiu de **8 casos de teste** (5 classes, sendo 1 método vazio) para **81 execuções automatizadas** distribuídas em **14 classes de teste** JUnit 5 + Mockito (apoiadas por `ApoioTeste`), atingindo **100% de aprovação (81 aprovados, 0 falhas, 0 erros e 0 ignorados)**.

| Classe de Teste | Foco Principal | Execuções | Uso de Mocks / Isolamento | Status |
| --- | --- | ---: | --- | --- |
| [`BatalhaMockitoTest`](../../test/br/edu/ucsal/pokesal/BatalhaMockitoTest.java) | Crítico, falha de precisão, ordem `InOrder` de eventos no turno, persistência ao final e resiliência a falha de repositório | 6 | `@Mock` (`GeradorAleatorio`, `RepositorioResultados`, `RegistroBatalha`, `PosBatalha`) | Aprovado |
| [`TerrenoMockitoTest`](../../test/br/edu/ucsal/pokesal/TerrenoMockitoTest.java) | Fronteiras diurnas/noturnas (`05:59:59`, `06:00:00`, `17:59:59`, `18:00:00`) do Asfalto Quente em `America/Bahia` | 1 | `@Mock` (`Clock`, `Supplier<Clima>`) + `@InjectMocks` | Aprovado |
| [`TorneioMockitoTest`](../../test/br/edu/ucsal/pokesal/TorneioMockitoTest.java) | Chaveamento eliminatório de 4 treinadores e avanço exclusivo dos vencedores até a final | 1 | `@Mock` (`FabricaBatalha` e 3 instâncias de `Batalha`) | Aprovado |
| [`RequisitosAutoraisMockitoTest`](../../test/br/edu/ucsal/pokesal/RequisitosAutoraisMockitoTest.java) | Pontuação com/sem bônus rápido (`RA03-01`), avisos ordenados vencedor→perdedor (`RA03-02`) e tolerância a falhas | 8 | `@Mock` (`CarteiraPontos`, `NotificadorResultado`, `RegistroBatalha`) + `@InjectMocks` | Aprovado |
| [`RegrasDominioTest`](../../test/br/edu/ucsal/pokesal/RegrasDominioTest.java) | Validações de `Pokesal`, `EfeitoStatus` (queimadura, paralisia, veneno), `ItemCura`, `Antidoto` e `Mochila` | 15 | `@Mock Pokesal` apenas nos testes isolados de status/itens; reais nos demais | Aprovado |
| [`Fase03BatalhaTest`](../../test/br/edu/ucsal/pokesal/Fase03BatalhaTest.java) | Limites de precisão/crítico, composição de dano, empate de velocidade, nocaute, exaustão de golpes e empate por status | 20 | Dublês determinísticos injetados via construtor | Aprovado |
| [`TorneioTest`](../../test/br/edu/ucsal/pokesal/TorneioTest.java) | Torneios completos de 4 e 8 participantes, restauração entre rodadas, ranking, falhas em todas as gravações e entradas inválidas | 12 | Dublês funcionais de `FabricaBatalha`, repositório e registro | Aprovado |
| [`ServicoTerrenoTest`](../../test/br/edu/ucsal/pokesal/ServicoTerrenoTest.java) | Consulta dinâmica de clima/horário e limites de cura do Canteiro Central (teto de HP e derrotados) | 6 | `Clock.fixed` e `AtomicReference<Clima>` | Aprovado |
| [`AdaptadoresAutoraisTest`](../../test/br/edu/ucsal/pokesal/AdaptadoresAutoraisTest.java) | Acúmulo em `CarteiraPontosMemoria` e formatação UTF-8 em `NotificadorResultadoConsole` | 2 | `ByteArrayOutputStream` / `PrintStream` em memória | Aprovado |
| [`RepositorioResultadosCsvTest`](../../test/br/edu/ucsal/pokesal/RepositorioResultadosCsvTest.java) | Escrita incremental UTF-8 em `resultados.csv`, escape de vírgulas/aspas e propagação de `IOException` | 2 | Diretório temporário isolado (`@TempDir`) | Aprovado |
| [`BatalhaTest`](../../test/br/edu/ucsal/pokesal/BatalhaTest.java) | Testes regressivos adaptados da Fase 02 (velocidade, limite de 2 itens, valores-limite de dano e HP mínimo) | 3 | Stubs neutros via `ApoioTeste.batalha` | Aprovado |
| [`PokesalTest`](../../test/br/edu/ucsal/pokesal/PokesalTest.java) | Limite de 4 golpes e consumo/restauração de usos dos golpes (antigo `CT-07` corrigido) | 2 | Objetos reais | Aprovado |
| [`ArenaTest`](../../test/br/edu/ucsal/pokesal/ArenaTest.java) | Multiplicadores de Asfalto Quente, Poça de Chuva e cura de 5% no Canteiro Central | 1 | `ApoioTeste.arena` com relógio/clima fixos | Aprovado |
| [`GolpeTest`](../../test/br/edu/ucsal/pokesal/GolpeTest.java) | Bloqueio por `IllegalStateException` ao exceder o máximo de usos de um golpe | 1 | Objeto real | Aprovado |
| [`TipoElementalTest`](../../test/br/edu/ucsal/pokesal/TipoElementalTest.java) | Tabela completa 3 × 3 de vantagens, desvantagens e neutralidade elemental | 1 | Objeto real | Aprovado |
| **Total** | — | **81** | — | **81 Aprovados** |

---

## 2. Registro de Bugs Encontrados e Corrigidos na Fase 03 {#registro-de-bugs-encontrados-e-corrigidos-na-fase-03}

Durante a implementação e execução da nova bateria de testes unitários, parametrizados e com Mockito na Fase 03, foram identificados e corrigidos **cinco defeitos reais** herdados da implementação ou da suíte da Fase 02:

### BUG-01 — Construtor de `Golpe` aceitava atributos inválidos, `NaN` e precisão fora do intervalo

- **Componente afetado:** [`Golpe.java`](../../src/br/edu/ucsal/pokesal/pokesal/Golpe.java)
- **Sintoma observado:** Ao testar valores de fronteira e inválidos (`-1`, `100.01`, `Double.NaN`, `Double.POSITIVE_INFINITY`), o construtor de `Golpe` criava instâncias inconsistentes sem lançar exceção, apesar de seu próprio Javadoc especificar `@throws IllegalArgumentException`. Também aceitava `nome` nulo/em branco, `poder < 0`, `maximosUsos <= 0` e enums nulos.
- **Teste(s) detector(es):** `Fase03BatalhaTest.rejeitaPrecisaoInvalida(double)`.
- **Correção aplicada:** Adicionada validação explícita no início do construtor de `Golpe` verificando `nome != null && !nome.isBlank()`, `poder >= 0`, `maximosUsos > 0`, `tipoElemental != null`, `statusAplicado != null`, `Double.isFinite(precisao)` em `[0, 100]` e `Double.isFinite(chanceStatus)` em `[0, 1]`.
- **Commit da correção:** `121259a` (*Novas Regras de Negócio*).

### BUG-02 — Laço infinito (`while (true)`) em `Batalha` quando todos os golpes tinham usos esgotados

- **Componente afetado:** [`Batalha.java`](../../src/br/edu/ucsal/pokesal/batalha/Batalha.java) (`executarAtaque` / `escolherGolpe`)
- **Sintoma observado:** Quando um Pokésal consumia todos os usos disponíveis de seus golpes (especialmente após falhas de precisão) e precisava atacar no turno seguinte, `escolherGolpe` entrava em `while (true)` pedindo opções de menu e rejeitando todas com *"Esse golpe não possui usos restantes"*, travando o simulador indefinidamente.
- **Teste(s) detector(es):** `Fase03BatalhaTest.golpesEsgotadosEncerramBatalhaSemLoopDeSelecao()`.
- **Correção aplicada:** Em `Batalha.executarAtaque(Treinador)`, antes de abrir o loop de seleção, verifica-se `treinador.getPokesal().getGolpes().stream().noneMatch(Golpe::podeUsar)`. Quando verdadeiro, o Pokésal sofre derrota por exaustão de golpes, registrando os eventos `ATAQUE` e `DANO` correspondentes e encerrando a batalha de forma limpa.
- **Commit da correção:** `121259a` (*Novas Regras de Negócio*).

### BUG-03 — Uso indevido de item inexistente na mochila e aceitação do mesmo `Treinador` nos dois lados da batalha

- **Componente afetado:** [`Batalha.java`](../../src/br/edu/ucsal/pokesal/batalha/Batalha.java) (construtor e `executarUsoItem`) e [`BatalhaTest.java`](../../test/br/edu/ucsal/pokesal/BatalhaTest.java)
- **Sintoma observado:**
  1. `Batalha.executarUsoItem(Treinador, Item)` aplicava o efeito de cura/antídoto sem validar se o `Item` realmente pertencia à `Mochila` daquele treinador (`Mochila.removerItem` apenas retornava `false` depois que o HP já havia sido curado) e sem validar se o treinador pertencia à batalha.
  2. O construtor de `Batalha` permitia `treinador1 == treinador2` (inclusive o antigo `CT-03` passava a mesma instância nas duas posições da partida, mascarando a independência dos contadores de itens).
- **Teste(s) detector(es):** `Fase03BatalhaTest.naoPermiteUsarItemAusenteDaMochila()`, `Fase03BatalhaTest.registraTodasAsCategoriasNaOrdemMesmoQuandoItemEPrimeiraAcao()` e revisão de `BatalhaTest.testUsoLimiteDeItensExcedido()`.
- **Correção aplicada:** O construtor de `Batalha` passou a rejeitar `treinador1 == treinador2` com `IllegalArgumentException`; `executarUsoItem` passou a validar o participante com `obterAdversario(treinador)` e exigir `treinador.getMochila().getItens().contains(item)` antes de aplicar qualquer efeito; e `BatalhaTest.testUsoLimiteDeItensExcedido` foi ajustado para usar dois treinadores distintos.
- **Commit da correção:** `121259a` (*Novas Regras de Negócio*).

### BUG-04 — Nocaute simultâneo por dano periódico de status deixava partida sem vencedor e permitia reexecução após o fim

- **Componente afetado:** [`Batalha.java`](../../src/br/edu/ucsal/pokesal/batalha/Batalha.java) (`iniciarBatalha`, `executarTurno`, `obterVencedor`, `finalizarBatalha`)
- **Sintoma observado:** Se ambos os Pokésal chegassem a `0` HP no mesmo fim de turno devido a veneno/queimadura, `verificarFimDaBatalha()` retornava `true`, porém `obterVencedor()` retornava `null`, quebrando o avanço obrigatório de vencedor em chaves eliminatórias de `Torneio` e impedindo a geração consistente de `ResultadoBatalha`. Adicionalmente, não havia trava contra chamar `executarTurno()` ou `iniciarBatalha()` novamente em uma batalha encerrada (o que duplicaria gravações no CSV e premiações).
- **Teste(s) detector(es):** `Fase03BatalhaTest.derrotaSimultaneaPorStatusUsaDesempateInjetadoUmaUnicaVez()`, `Fase03BatalhaTest.nocauteInterrompeTurnoEPersisteUmaVezComIdentidadesETurnos()` e `BatalhaMockitoTest.testResultadoSalvoAoFimDaBatalha()`.
- **Correção aplicada:** Criado `finalizarBatalha()` idempotente, que resolve eventual derrota simultânea uma única vez por meio do `GeradorAleatorio` injetado, fixa `vencedor` e `ResultadoBatalha`, persiste/aciona `PosBatalha` uma só vez e faz `iniciarBatalha()`/`executarTurno()` lançarem `IllegalStateException` se invocados após o término.
- **Commit da correção:** `121259a` (*Novas Regras de Negócio*) e integração com `PosBatalha` em `6147929` (*update mockito junit test*).

### BUG-05 — Teste vazio (`CT-07`) com falso positivo e `Random` estático bloqueando determinismo

- **Componente afetado:** [`PokesalTest.java`](../../test/br/edu/ucsal/pokesal/PokesalTest.java), [`Golpe.java`](../../src/br/edu/ucsal/pokesal/pokesal/Golpe.java) e [`Batalha.java`](../../src/br/edu/ucsal/pokesal/batalha/Batalha.java)
- **Sintoma observado:** Na Fase 02, `PokesalTest.testMaximoDeUsoGolpes` tinha corpo vazio (sendo reportado como aprovado pelo JUnit sem validar nenhum requisito e gerando os alertas `java:S2699` e `java:S1186` no SonarQube). Além disso, `Golpe` e `Batalha` instanciavam `private static final Random RANDOM = new Random()`, impedindo testes determinísticos de precisão, crítico, status e desempate.
- **Teste(s) detector(es):** `PokesalTest.testMaximoDeUsoGolpes()`, `BatalhaMockitoTest.testAcertoCriticoComGeradorMockado()` e `BatalhaMockitoTest.testGolpeErraQuandoPrecisaoFalha()`.
- **Correção aplicada:** Preenchido `PokesalTest.testMaximoDeUsoGolpes()` com asserções de decremento (`consumirUso`) e restauração (`resetarUsosDosGolpes`), removidas todas as instâncias estáticas de `Random` das classes de domínio e introduzida a injeção de `GeradorAleatorio` via construtor.
- **Commit da correção:** `121259a` (*Novas Regras de Negócio*) e suíte Mockito em `6147929` (*update mockito junit test*).

---

## 3. Evolução da Cobertura Estrutural (Fase 02 vs. Fase 03)

No escopo das **23 classes executáveis de regras de negócio** (`arena`, `batalha`, `enums`, `item`, `pokesal`, `resultado`, `torneio` e `treinador`), todas superaram individualmente os limites mínimos de **80% de linhas** e **70% de branches** impostos por `jacoco:check`:

| Pacote / Escopo | Linhas Cobertas (Fase 03) | Cobertura de Linhas | Branches Cobertos (Fase 03) | Cobertura de Branches |
| --- | ---: | ---: | ---: | ---: |
| `br.edu.ucsal.pokesal.arena` | 38 / 38 | 100,0% | 22 / 22 | 100,0% |
| `br.edu.ucsal.pokesal.batalha` | 221 / 242 | 91,3% | 110 / 138 | 79,7% |
| `br.edu.ucsal.pokesal.enums` | 26 / 26 | 100,0% | 14 / 14 | 100,0% |
| `br.edu.ucsal.pokesal.item` | 20 / 20 | 100,0% | 8 / 8 | 100,0% |
| `br.edu.ucsal.pokesal.pokesal` | 150 / 153 | 98,0% | 81 / 96 | 84,4% |
| `br.edu.ucsal.pokesal.resultado` | 32 / 32 | 100,0% | 4 / 4 | 100,0% |
| `br.edu.ucsal.pokesal.torneio` | 53 / 53 | 100,0% | 21 / 22 | 95,5% |
| `br.edu.ucsal.pokesal.treinador` | 22 / 22 | 100,0% | 4 / 4 | 100,0% |
| **Total Escopo de Negócio (Fase 03)** | **562 / 586** | **95,90%** | **264 / 308** | **85,71%** |
| *Referência histórica global (Fase 02)* | *196 / 490* | *40,00%* | *70 / 240* | *29,17%* |

O relatório HTML navegável por classe e linha encontra-se em [`jacoco-fase03/index.html`](jacoco-fase03/index.html).

---

## 4. Histórico Preservado da Fase 02 (CT-01 a CT-08)

Os registros abaixo preservam o estado original documentado na Fase 02 (antes das expansões e correções da Fase 03):

| ID (Fase 02) | Classe e Método | Situação na Fase 02 | Evolução na Fase 03 |
| --- | --- | --- | --- |
| CT-01 | `ArenaTest.testEfeitoTerrenoEstacionamentoUCSal` | Aprovado | Mantido e complementado por `TerrenoMockitoTest` e `ServicoTerrenoTest` |
| CT-02 | `BatalhaTest.testOrdemDeAtaquePorVelocidade` | Aprovado | Mantido e complementado por empate e paralisia |
| CT-03 | `BatalhaTest.testUsoLimiteDeItensExcedido` | Aprovado (usava mesmo treinador nos 2 lados) | Corrigido para dois treinadores distintos (`BUG-03`) |
| CT-04 | `BatalhaTest.testCalculoDanoBoundaryValues` | Aprovado | Mantido (compatibilidade de reflexão preservada) e expandido com crítico/terreno/tipo |
| CT-05 | `GolpeTest.testMaximoDeUsoGolpes` | Aprovado | Mantido e complementado por erro de precisão e exaustão (`BUG-02`) |
| CT-06 | `PokesalTest.testMaximoDeGolpes` | Aprovado | Mantido e complementado por validação no construtor |
| CT-07 | `PokesalTest.testMaximoDeUsoGolpes` | Vazio (falso positivo) | Preenchido com asserções reais de consumo e restauração (`BUG-05`) |
| CT-08 | `TipoElementalTest.testVantagemElemental` | Aprovado | Mantido (9 combinações da matriz 3 × 3) |
