# Suíte mínima de testes

A suíte contém **19 métodos de teste**, o mínimo explicitamente pedido:

| Parte | Obrigatórios | Autorais | Total |
| --- | ---: | ---: | ---: |
| Parte 1 | 5 | 2 | 7 |
| Parte 2 | 8 | 4 (dois por requisito) | 12 |
| Total | 13 | 6 | **19** |

Não há testes extras nem casos parametrizados. Os cenários de limites foram
integrados aos testes dos respectivos requisitos, preservando a cobertura.

| Requisito da parte 2 | Teste | Colaborador mockado | Resultado esperado | Resultado obtido |
| --- | --- | --- | --- | --- |
| Crítico | `BatalhaMockitoTest.testAcertoCriticoComGeradorMockado` | Gerador e registro | Dano 30 para crítico e 20 para normal | Aprovado |
| Precisão | `BatalhaMockitoTest.testGolpeErraQuandoPrecisaoFalha` | Gerador e registro | Dano zero e HP inalterado | Aprovado |
| Terreno diurno | `TerrenoMockitoTest.testAsfaltoQuenteApenasDuranteODia` | Clock e clima | Fogo ×1,15 somente durante o dia ensolarado | Aprovado |
| Registro do turno | `BatalhaMockitoTest.testSequenciaDeEventosNoTurno` | Gerador e registro | Ataque, dano, status, terreno e item, nessa ordem | Aprovado |
| Persistência | `BatalhaMockitoTest.testResultadoSalvoAoFimDaBatalha` | Repositório e pós-batalha | Capturar vencedor, perdedor, Pokésal e turnos | Aprovado |
| Falha de persistência | `BatalhaMockitoTest.testFalhaNoRepositorioNaoInvalidaBatalha` | Repositório e registro | Registrar erro e preservar vencedor | Aprovado |
| Batalha em andamento | `BatalhaMockitoTest.testRepositorioNaoChamadoSeBatalhaNaoTerminou` | Repositório | Nenhuma gravação | Aprovado |
| Chaveamento | `TorneioMockitoTest.testChaveamentoTorneioAvancaVencedores` | Fábrica e batalhas | Apenas vencedores avançam | Aprovado |
| Pontuação com bônus | `RequisitosAutoraisMockitoTest.testPremioComBonusAteTresTurnos` | Carteira | 150 pontos ao vencedor e nenhum ao perdedor | Aprovado |
| Pontuação sem bônus | `RequisitosAutoraisMockitoTest.testPremioSemBonusDepoisDeTresTurnos` | Carteira | 100 pontos ao vencedor | Aprovado |
| Notificação | `RequisitosAutoraisMockitoTest.testNotificaAmbosComResultadoCompleto` | Notificador | Avisar vencedor e perdedor, com resultado completo | Aprovado |
| Falha de notificação | `RequisitosAutoraisMockitoTest.testFalhaNoPrimeiroAvisoNaoImpedeAvisoAoPerdedor` | Notificador e registro | Falha no primeiro aviso não impede o segundo | Aprovado |

Os testes da parte 2 usam `MockitoExtension`. São mantidos `@Mock`,
`@InjectMocks`, retornos sequenciais, `times`, `never`, `atLeastOnce`, `InOrder`,
`ArgumentCaptor`, `doThrow`, `any` e `eq`.

Mocks isolam aleatoriedade, tempo, persistência e saída. A classe sob teste,
Pokésal, golpes e matriz elemental usam implementações reais. No teste de torneio,
as batalhas são mockadas para controlar os vencedores.
`any` configura comportamento independente do conteúdo; `eq` fixa um argumento
quando outro usa matcher ou captura. Valores exatos verificam pontos e chaves.

## Testes da parte 1 e verificações de limites

| Teste | Requisito e verificações incluídas |
| --- | --- |
| `TipoElementalTest.testVantagemElemental` | Todas as nove relações elementais |
| `ArenaTest.testEfeitoTerrenoEstacionamentoUCSal` | Bônus e cura dos três terrenos |
| `BatalhaTest.testOrdemDeAtaquePorVelocidade` | Maior SPD, empate e participantes válidos |
| `BatalhaTest.testUsoLimiteDeItensExcedido` | Limite de dois itens, mochila e ausência de cura em derrotados |
| `BatalhaTest.testCalculoDanoBoundaryValues` | Limites de HP/ATK/DEF, overflow e rejeição de atributos/operações inválidos |
| `PokesalTest.testMaximoDeGolpes` | Autoral: limite de quatro golpes |
| `GolpeTest.testMaximoDeUsoGolpes` | Autoral: esgotamento e restauração de usos |

Na parte 2, a falha de precisão também verifica atributos inválidos do golpe;
o teste de eventos verifica dano progressivo do veneno, queimadura, paralisia e
expiração; o teste de chaveamento também rejeita participantes inválidos.
O total de métodos diminuiu de 27 para 19, sem eliminar essas verificações.

## Resultado obtido em 12/11/2026

Comando executado na raiz:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/testar.ps1 -Offline
```

**19 testes aprovados, zero falhas, zero erros e zero ignorados.**
**Checkstyle: zero violações**, incluindo código de produção e testes.
JaCoCo: **94,46% de linhas (597/632)** e **83,55% de branches (254/304)**.
Todas as 23 classes executáveis do escopo passaram nas metas individuais de
80% de linhas e 70% de branches. Somente `app` e `infraestrutura` permanecem
excluídos da medição, como antes desta revisão.

- [JaCoCo atual, por classe](jacoco-atual/index.html).
- [Log Maven completo](evidencias/atual/maven-verify.txt).
- [Resultado Checkstyle](evidencias/atual/checkstyle.xml).
- [Verificação do console](evidencias/atual/console.json): modos avulso, torneio
  de 4 e torneio de 8 concluídos, com 1/3/7 resultados e 2/6/14 avisos.
- Os XMLs Surefire da execução ficam em `evidencias/atual/TEST-*.xml`.

A primeira execução baixa dependências em `.build/m2`. Com o cache preenchido,
acrescente `-Offline` ao comando do script para executar sem rede.

## Relatórios anteriores e registro de bugs

Os relatórios JaCoCo, SonarQube e logs de 81 testes são históricos. O SonarQube
não foi executado novamente nesta revisão: suas métricas não representam o
código refatorado. Não se deve substituir as métricas do Sonar por essas do JaCoCo.
O [registro anterior de bugs e commits](RELATORIO-TESTES.md) foi preservado.

| Bug desta revisão | Correção | Teste de regressão | Commit |
| --- | --- | --- | --- |
| Veneno fixo contrariava a progressão pedida no enunciado | Contador de turnos de veneno; mantém o primeiro dano e aumenta os seguintes | `BatalhaMockitoTest.testSequenciaDeEventosNoTurno` | Alterações locais, ainda sem commit |
| Soma inteira no dano podia sofrer overflow antes da conversão | Converter ATK antes da soma e limitar o resultado a Integer.MAX_VALUE | `BatalhaTest.testCalculoDanoBoundaryValues`, incluindo ATK e poder máximos | Alterações locais, ainda sem commit |

O [relatório de refatoração](../REFATORACAO.md) descreve as mudanças e a
inversão de dependência. As regras autorais existentes foram preservadas;
não foram criados novos requisitos autorais nesta revisão.
