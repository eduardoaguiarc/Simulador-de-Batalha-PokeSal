# Fase 03 — requisitos autorais, Mockito e JaCoCo

> **Suíte reduzida:** consulte a [suíte mínima atual](SUITE-MINIMA.md). As contagens, resultados e coberturas abaixo descrevem a execução histórica de 81 testes, anterior à suíte atual de 19 testes. Referências a testes removidos são históricas.

## 4.2 — Dois requisitos autorais novos

Os três requisitos das fases anteriores (quatro golpes, precisão e usos limitados)
continuam existindo. Os dois requisitos **novos da Fase 03** são:

### RA03-01 — Pontuação por vitória e bônus por vitória rápida

Ao concluir uma batalha com pelo menos um turno disputado, creditar **100 pontos
ao vencedor**, mais **50 pontos se a vitória ocorrer em até três turnos**.
O perdedor não recebe pontos. Uma batalha encerrada sem disputar turnos não gera
premiação. Uma falha na carteira registra `ERRO_PREMIACAO` e preserva o resultado
e os avisos aos jogadores.

- Regra: `ServicoPosBatalha.processar`.
- Colaborador externo mockável: `CarteiraPontos`, injetado pelo construtor.
- Adaptador: `CarteiraPontosMemoria`, compartilhado entre as batalhas da sessão.
  O console mostra os saldos ao encerrar a batalha avulsa ou o torneio. Os pontos
  não persistem entre execuções nem alteram o ranking por vitórias do torneio.
- Testes Mockito: `testPremioComBonusAteTresTurnos`,
  `testPremioSemBonusDepoisDeTresTurnos`,
  `testSemPremioQuandoNenhumTurnoFoiDisputado` e
  `testFalhaNaPremiacaoPreservaResultadoENotificacoes`.

### RA03-02 — Aviso do resultado aos dois participantes

Ao concluir uma batalha, enviar o resultado completo primeiro ao vencedor e
depois ao perdedor, uma tentativa por destinatário. O aviso contém identificação
da batalha, vencedor, perdedor, Pokésal utilizados e número de turnos. Uma falha
no primeiro envio não impede o segundo; falhas geram `ERRO_NOTIFICACAO` sem
desfazer o resultado nem a premiação. Não há repetição automática dos envios.

- Regra: `ServicoPosBatalha.processar` e `notificar`.
- Colaborador externo mockável: `NotificadorResultado`, injetado pelo construtor.
- Adaptador: `NotificadorResultadoConsole`, que apresenta um aviso individual no
  terminal compartilhado dos jogadores. Nenhuma integração de e-mail é necessária.
- Testes Mockito: `testNotificaAmbosComResultadoCompleto`,
  `testFalhaNoPrimeiroAvisoNaoImpedeAvisoAoPerdedor` e
  `testFalhaNoSegundoAvisoNaoInvalidaPremiacao`.

`Main` injeta o serviço via `PosBatalha` tanto na batalha avulsa quanto na fábrica
do torneio. `Batalha.finalizarBatalha` o chama após a tentativa de salvar o CSV,
inclusive se a persistência falhar. A proteção contra finalizar a mesma batalha
duas vezes também impede repetir os créditos e avisos. O serviço não mantém um
histórico próprio de deduplicação: a garantia de chamada única pertence à batalha.
O construtor anterior de `Batalha` foi preservado para clientes que não utilizam
essas ações adicionais.

O teste `testBatalhaRealPremiaEAvisaUmaUnicaVezMesmoComFalhaNoCsv` exercita as duas
regras com uma batalha real, carteira e notificador mockados. Há **oito testes
Mockito autorais**, além dos testes dos adaptadores reais.

## 4.3 — Suíte obrigatória

Todos os novos arquivos de teste usam JUnit 5 e
`@ExtendWith(MockitoExtension.class)`. Os testes anteriores foram preservados.

| Teste obrigatório | Classe | Verificação |
| --- | --- | --- |
| `testAcertoCriticoComGeradorMockado()` | `BatalhaMockitoTest` | Dano normal 20, crítico 30; sequência de sorteios e limite exclusivo 0,0625 |
| `testGolpeErraQuandoPrecisaoFalha()` | `BatalhaMockitoTest` | Evento de dano zero, HP inalterado, uso consumido, sem sorteio de crítico |
| `testAsfaltoQuenteApenasDuranteODia()` | `TerrenoMockitoTest` | Relógio mockado em 05:59:59, 06:00, 17:59:59 e 18:00 no fuso da Bahia |
| `testSequenciaDeEventosNoTurno()` | `BatalhaMockitoTest` | `InOrder`: ataque, dano, status, terreno, item; inclui item como primeira ação |
| `testResultadoSalvoAoFimDaBatalha()` | `BatalhaMockitoTest` | `ArgumentCaptor`: vencedor, perdedor, ID e turnos; gravação única |
| `testFalhaNoRepositorioNaoInvalidaBatalha()` | `BatalhaMockitoTest` | `doThrow`: vencedor e resultado preservados; erro registrado |
| `testRepositorioNaoChamadoSeBatalhaNaoTerminou()` | `BatalhaMockitoTest` | `never()` no repositório e no serviço de pós-batalha |
| `testChaveamentoTorneioAvancaVencedores()` | `TorneioMockitoTest` | Batalhas mockadas devolvem resultados; só vencedores disputam a final |

### Recursos Mockito aplicados

| Recurso | Uso concreto |
| --- | --- |
| `@Mock` | Gerador, repositório, registro, relógio, clima, fábrica, batalhas, carteira e notificador |
| `@InjectMocks` | Construtores de `ServicoTerrenoClimatico` e `ServicoPosBatalha` |
| `when(...).thenReturn(...)` | Sorteios, relógio, clima, resultados e vencedores |
| Retornos em sequência | Crítico seguido de golpe normal; quatro horários nas fronteiras do dia |
| `verify(..., times(...))` | Quantidade de sorteios, gravações, créditos e avisos |
| `verify(..., never())` | Persistência antes do fim, pontos ao perdedor, cura em derrotado |
| `verify(..., atLeastOnce())` | Consulta de clima e envio de eventos ao registro |
| `InOrder` | Categorias de eventos, persistência antes do pós-batalha, destinatários e chaves |
| `ArgumentCaptor` | Resultado persistido, avisos enviados e eventos de dano/erro |
| `doThrow(...)` | Falha de CSV, carteira e ambos os destinatários do notificador |
| `any()` e `eq()` | Objeto de resultado variável com destinatário fixo; detalhes abaixo |

**Quando usar matchers:** `any(ResultadoBatalha.class)` é adequado para configurar
uma falha independente do conteúdo do resultado, ou garantir com `never()` que
nenhum resultado foi persistido. `eq(1)` fixa o destinatário enquanto o outro
argumento usa `any()` ou `capture()`. Todos os argumentos de uma mesma chamada
devem ser matchers quando algum deles é matcher. `argThat` seleciona a categoria
do evento quando o texto não é o objeto da verificação.

**Quando usar valores exatos:** as chaves do torneio, IDs e pontos de premiação
são parte da regra, portanto usam valores exatos (por exemplo,
`verify(carteira).creditar(1, 150)` e `when(fabrica.criar(3, t2, t3))`). Os objetos
de resultado são capturados e inspecionados; um `any()` isolado não comprovaria
que o vencedor, o perdedor e os turnos estão corretos.

## 4.4 — Cobertura e evidências

Execução de 02/11/2026: **81 testes, zero falhas, zero erros e zero ignorados**.
Maven 3.9.16, JDK 25.0.4.1 compilando com `--release 21`, JUnit 5.14.0,
Mockito 5.23.0 e JaCoCo 0.8.16. O BOM do JUnit mantém API, engine e launcher
alinhados; Mockito e JaCoCo são carregados como agentes na JVM de testes.

| Escopo de negócio | Cobertos / total | Cobertura |
| --- | --- | --- |
| Linhas | 562 / 586 | **95,90%** |
| Branches | 264 / 308 | **85,71%** |
| Serviço dos requisitos autorais — linhas | 24 / 24 | **100%** |
| Serviço dos requisitos autorais — branches | 4 / 4 | **100%** |

A meta é aplicada **por classe**, não apenas ao agregado: `mvn verify` falha se
uma classe de negócio ficar abaixo de **80% das linhas ou 70% dos branches**.
Todas as 23 classes executáveis do escopo passaram. `Batalha` ficou com 91,32%
das linhas e 79,71% dos branches; `Golpe` atingiu 70% dos branches. Classes sem
branches não têm decisões a cobrir.

O escopo inclui os pacotes `arena`, `batalha`, `enums`, `item`, `pokesal`,
`resultado`, `torneio` e `treinador`. Somente `app` (entrada e menus de console)
e `infraestrutura` (adaptadores de saída/persistência) são excluídos da medição
de negócio. Os adaptadores novos também têm testes, embora fora dessa meta.

- [Relatório HTML completo do JaCoCo](jacoco-fase03/index.html).
- [Contadores XML](jacoco-fase03/jacoco.xml) e [CSV](jacoco-fase03/jacoco.csv).
- [Resumo da execução Maven](evidencias/fase-03-mockito-maven.txt).
- [Verificação do console nos três modos](evidencias/fase-03-autorais-console.txt):
  batalha avulsa e torneios de 4/8 participantes encerraram com 1/3/7 resultados
  CSV, 2/6/14 avisos e saldos de pontos exibidos. Essa verificação de integração
  foi executada separadamente e não entra nos 81 testes nem na medição JaCoCo.

### Reproduzir

Na raiz do repositório, com JDK 21+ e Maven:

```powershell
mvn -f Simulador-de-Batalha-PokeSal-main/pom.xml clean verify
New-Item -ItemType Directory -Force Simulador-de-Batalha-PokeSal-main/docs/testes/jacoco-fase03
Copy-Item -Recurse -Force Simulador-de-Batalha-PokeSal-main/target/site/jacoco/* `
  Simulador-de-Batalha-PokeSal-main/docs/testes/jacoco-fase03
```

O primeiro comando executa testes, gera o relatório em `target/site/jacoco` e
verifica as metas. O relatório incluído neste repositório é a cópia desta execução
limpa; os relatórios antigos continuam identificados como evidências históricas.
O Maven desta máquina foi localizado no cache do wrapper e as dependências foram
baixadas para `.build/m2`; esses caminhos locais não são necessários para reproduzir.

Configuração baseada na [documentação oficial de `jacoco:check`](https://www.jacoco.org/jacoco/trunk/doc/check-mojo.html).
