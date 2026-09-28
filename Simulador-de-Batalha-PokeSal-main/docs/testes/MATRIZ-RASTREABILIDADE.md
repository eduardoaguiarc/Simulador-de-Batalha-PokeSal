# Matriz de rastreabilidade de requisitos e testes

## Requisito → implementação → teste

| ID | Critério e origem | Implementação | Caso executado | Situação e lacuna |
| --- | --- | --- | --- | --- |
| RF-01 | README, partida: cadastrar dois treinadores e escolher um único inicial dentre seis opções em dois grupos. | [Main](../../src/br/edu/ucsal/pokesal/app/Main.java): `cadastrarTreinador`, `escolherPokesalInicial` | — | Sem teste: cadastro, grupos e seis escolhas. |
| RF-02 | README, partida/terreno: selecionar uma das três arenas antes da batalha. | [Main](../../src/br/edu/ucsal/pokesal/app/Main.java): `escolherArena` | — | Sem teste: seleção pelo console e arena atribuída à batalha. |
| RF-03 | README, atributos: representar HP, ATK, DEF, SPD e tipo de cada inicial. | [Main](../../src/br/edu/ucsal/pokesal/app/Main.java): `criarPokesal`; [Pokesal](../../src/br/edu/ucsal/pokesal/pokesal/Pokesal.java): construtor e getters | — | Sem teste: valores dos seis iniciais e estado inicial completo. |
| RF-04 | README, vantagens: retornar a tabela 3 × 3 de multiplicadores 2,0 / 0,5 / 1,0. | [TipoElemental](../../src/br/edu/ucsal/pokesal/enums/TipoElemental.java): `calcularMultiplicadorContra` | [CT-08](RELATORIO-TESTES.md#ct-08--tabela-de-vantagens-elementais) | Verificado: nove combinações. |
| RF-05 | README, terreno: multiplicador do Asfalto de 1,15 para Fogo e 1,0 para Água/Planta. | [Arena](../../src/br/edu/ucsal/pokesal/arena/Arena.java): `calcularMultiplicadorDano` | [CT-01](RELATORIO-TESTES.md#ct-01--efeitos-dos-terrenos) | Verificado: retorno para os três tipos; composição no dano pertence a RI-01. |
| RF-06 | README, terreno: multiplicador da Poça de 1,10 para Água e 1,0 para Fogo/Planta. | [Arena](../../src/br/edu/ucsal/pokesal/arena/Arena.java): `calcularMultiplicadorDano` | [CT-01](RELATORIO-TESTES.md#ct-01--efeitos-dos-terrenos) | Verificado: retorno para os três tipos; composição no dano pertence a RI-01. |
| RF-07 | README, terreno: canteiro cura Planta em 5% do HP máximo, sem ultrapassar o teto nem recuperar derrotados. | [Arena](../../src/br/edu/ucsal/pokesal/arena/Arena.java): `aplicarEfeitoFimDoTurno`; [Pokesal](../../src/br/edu/ucsal/pokesal/pokesal/Pokesal.java): `recuperarHp` | [CT-01](RELATORIO-TESTES.md#ct-01--efeitos-dos-terrenos) | Parcial: HP 80 → 85 de um Planta com máximo 100; faltam teto, derrotados, outros tipos e arredondamento. |
| RF-08 | README, turnos: maior velocidade efetiva determina quem age primeiro. | [Batalha](../../src/br/edu/ucsal/pokesal/batalha/Batalha.java): `determinarPrimeiroAtacante` | [CT-02](RELATORIO-TESTES.md#ct-02--ordem-por-velocidade) | Parcial: treinador 1 com velocidade 100 contra 10; faltam ordem invertida e status. |
| RF-09 | README, turnos: empates de velocidade têm desempate aleatório. | [Batalha](../../src/br/edu/ucsal/pokesal/batalha/Batalha.java): `determinarPrimeiroAtacante` | — | Sem teste: velocidades iguais. |
| RF-10 | README, partida/turnos: encerrar ao zerar HP, interromper ações restantes e apresentar resultado. | [Batalha](../../src/br/edu/ucsal/pokesal/batalha/Batalha.java): `iniciarBatalha`, `executarTurno`, `verificarFimDaBatalha`, `obterVencedor` | — | Sem teste: CT-04 confere somente HP zero, não o encerramento. |
| RF-11 | README, turnos: aplicar terreno e dano de status ao final de cada turno enquanto a batalha continuar. | [Batalha](../../src/br/edu/ucsal/pokesal/batalha/Batalha.java): `executarTurno`, `aplicarEfeitosFimDoTurno` | — | Sem teste: CT-01 chama diretamente a arena, sem executar o turno. |
| RF-12 | README, status: queimadura reduz ataque pela metade e causa dano ao fim do turno. | [EfeitoStatus](../../src/br/edu/ucsal/pokesal/pokesal/EfeitoStatus.java): `calcularAtaqueEfetivo`, `aplicarFimDoTurno` | — | Sem teste: alvo queimado e impacto no dano/HP. |
| RF-13 | README, status: veneno causa dano ao fim do turno. | [EfeitoStatus](../../src/br/edu/ucsal/pokesal/pokesal/EfeitoStatus.java): `aplicarFimDoTurno` | — | Sem teste: alvo envenenado. |
| RF-14 | README, status: paralisia reduz velocidade pela metade e influencia a ordem. | [EfeitoStatus](../../src/br/edu/ucsal/pokesal/pokesal/EfeitoStatus.java): `calcularVelocidadeEfetiva`; [Batalha](../../src/br/edu/ucsal/pokesal/batalha/Batalha.java): `determinarPrimeiroAtacante` | — | Sem teste: alvo paralisado. |
| RF-15 | README, mochila: iniciar com Potion, Super Potion e Antidote. | [Main](../../src/br/edu/ucsal/pokesal/app/Main.java): `montarMochila` | — | Sem teste: CT-03 monta uma mochila própria, diferente da inicial. |
| RF-16 | README, mochila: poções curam 20/40 HP respeitando o HP máximo. | [Main](../../src/br/edu/ucsal/pokesal/app/Main.java): constantes de cura; [ItemCura](../../src/br/edu/ucsal/pokesal/item/ItemCura.java): `aplicarEfeito`; [Pokesal](../../src/br/edu/ucsal/pokesal/pokesal/Pokesal.java): `recuperarHp` | — | Sem teste: CT-03 usa cura em HP cheio e não confere HP; faltam cura efetiva e teto. |
| RF-17 | README, mochila: Antidote remove status aplicado. | [Antidoto](../../src/br/edu/ucsal/pokesal/item/Antidoto.java): `aplicarEfeito`; [Pokesal](../../src/br/edu/ucsal/pokesal/pokesal/Pokesal.java): `removerStatus` | — | Sem teste: CT-03 usa antídoto sem status prévio e sem asserção de status. |
| RF-18 | README, mochila: no máximo dois itens por treinador em cada batalha. | [Batalha](../../src/br/edu/ucsal/pokesal/batalha/Batalha.java): `executarUsoItem`, `registrarUsoItem`, `quantidadeItensUsados` | [CT-03](RELATORIO-TESTES.md#ct-03--limite-de-itens) | Parcial: terceiro uso rejeitado; mesmo treinador nas duas posições, sem verificar independência dos contadores. |
| RF-19 | README, mochila: usar item ocupa a ação e retira o item da mochila. | [Batalha](../../src/br/edu/ucsal/pokesal/batalha/Batalha.java): `realizarAcao`, `executarUsoItem`; [Mochila](../../src/br/edu/ucsal/pokesal/treinador/Mochila.java): `removerItem` | — | Sem teste: ausência de asserções sobre turno e inventário. |
| RE-01 | README, adicional 1: até quatro golpes por Pokésal; iniciais começam com quatro opções. | [Pokesal](../../src/br/edu/ucsal/pokesal/pokesal/Pokesal.java): construtor, `adicionarGolpe`; [Main](../../src/br/edu/ucsal/pokesal/app/Main.java): `criarGolpesIniciais` | [CT-06](RELATORIO-TESTES.md#ct-06--limite-de-quatro-golpes) | Parcial: quinta inclusão rejeitada; faltam lista no construtor e golpes dos iniciais. |
| RE-02 | README, adicional 2: falha de precisão não causa dano. | [Golpe](../../src/br/edu/ucsal/pokesal/pokesal/Golpe.java): `testarAcerto`; [Batalha](../../src/br/edu/ucsal/pokesal/batalha/Batalha.java): `executarAtaque` | — | Sem teste: HP após acerto/erro. |
| RE-03 | README, adicional 3: usos limitados, consumo mesmo no erro e bloqueio da escolha de golpe esgotado. | [Golpe](../../src/br/edu/ucsal/pokesal/pokesal/Golpe.java): `consumirUso`, `podeUsar`; [Batalha](../../src/br/edu/ucsal/pokesal/batalha/Batalha.java): `executarAtaque`, `escolherGolpe` | [CT-05](RELATORIO-TESTES.md#ct-05--esgotamento-dos-usos-de-um-golpe) | Parcial: quarta chamada rejeitada para máximo três; faltam erro de precisão, contador e escolha no console. CT-07 vazio não conta. |
| RI-01 | Código: dano usa ATK efetivo + poder − DEF, mínimo base 1, multiplicadores de tipo/terreno e arredondamento. | [Batalha](../../src/br/edu/ucsal/pokesal/batalha/Batalha.java): `calcularDano` | [CT-04](RELATORIO-TESTES.md#ct-04--limites-do-dano-e-do-hp) | Parcial: dois limites inferiores com fatores neutros; faltam composição, valores positivos e arredondamento. |
| RI-02 | Código: dano superior ao HP disponível reduz HP a zero, sem valor negativo. | [Pokesal](../../src/br/edu/ucsal/pokesal/pokesal/Pokesal.java): `receberDano` | [CT-04](RELATORIO-TESTES.md#ct-04--limites-do-dano-e-do-hp) | Verificado: HP 1 recebe dano 9999 e termina em 0. |
| RI-03 | Código: menus rejeitam entradas não inteiras ou fora do intervalo e solicitam nova opção. | [Main](../../src/br/edu/ucsal/pokesal/app/Main.java): `lerOpcao`; [Batalha](../../src/br/edu/ucsal/pokesal/batalha/Batalha.java): `lerOpcao` | — | Sem teste: entradas inválidas seguidas de válidas. |
| RI-04 | Código: construtores de Pokésal/Golpe rejeitam atributos inválidos, nomes vazios e tipos nulos. | [Pokesal](../../src/br/edu/ucsal/pokesal/pokesal/Pokesal.java) e [Golpe](../../src/br/edu/ucsal/pokesal/pokesal/Golpe.java): construtores | — | Sem teste: entradas inválidas e exceções esperadas. |

## Teste → requisitos

| Caso | Requisitos com verificação direta | Evidência |
| --- | --- | --- |
| CT-01 | RF-05, RF-06, RF-07 | `ArenaTest.testEfeitoTerrenoEstacionamentoUCSal` |
| CT-02 | RF-08 | `BatalhaTest.testOrdemDeAtaquePorVelocidade` |
| CT-03 | RF-18 | `BatalhaTest.testUsoLimiteDeItensExcedido` |
| CT-04 | RI-01, RI-02 | `BatalhaTest.testCalculoDanoBoundaryValues` |
| CT-05 | RE-03 | `GolpeTest.testMaximoDeUsoGolpes` |
| CT-06 | RE-01 | `PokesalTest.testMaximoDeGolpes` |
| CT-07 | Nenhum: método vazio | `PokesalTest.testMaximoDeUsoGolpes` |
| CT-08 | RF-04 | `TipoElementalTest.testVantagemElemental` |

Os resultados por método estão no [extrato JUnit](evidencias/junit-jupiter.xml).

## Síntese e manutenção

Foram catalogados **26 critérios**: **4 verificados, 6 parciais e 16 sem teste**.