# Relatório Comparativo SonarQube (Fase 02 vs. Fase 03) e Integração Contínua

## 1. Identificação da Execução (Fase 03)

- **Servidor:** SonarQube Community Build `26.9.0.129388` (`http://localhost:9000`)
- **Scanner:** `org.sonarsource.scanner.maven:sonar-maven-plugin:5.0.0.4389` (Maven 3.9.16 / Java 21)
- **Projeto:** `Simulador-de-Batalha-PokeSal` (versão `3.0.0`, comparada contra `PREVIOUS_VERSION = 2.0.0`)
- **Data/hora do processamento:** `2026-11-02T18:22:06-0300` (`analysisId: 1c8ba4ac-5776-445c-87d9-f4ae9e29c76f`)
- **Escopo analisado na Fase 03:** 28 arquivos Java de produção em `src/` e 15 arquivos Java em `test/` (14 classes de teste JUnit 5/Mockito + `ApoioTeste`).
- **Quality Gate:** **OK (PASSED)** — *Clean as You Code compliant*, `0` novas violações bloqueantes no período incremental.

---

## 2. Comparativo Obrigatório: Fase 02 vs. Fase 03

Os valores da **Fase 02** provêm da análise histórica registrada em [`evidencias/fase02/metricas.json`](evidencias/fase02/metricas.json) (commit `01dc18c` / `cdaec42`, 14 arquivos de produção e 5 classes de teste), complementada pela medição de `Duplications` sobre a mesma revisão no SonarQube `26.9.0.129388`. Os valores da **Fase 03** provêm de [`evidencias/metricas.json`](evidencias/metricas.json).

| Métrica Obrigatória | Fase 02 (`v2.0.0`) | Fase 03 (`v3.0.0`) | Variação ||
| --- | ---: | ---: | ---: | --- |
| **Code Smells** | 67 | **80** | +13 (+19,4%) |
| **Bugs** | 1 | **1** | 0 (0,0%) |
| **Vulnerabilities** | 0 | **0** | 0 (0,0%) |
| **Coverage %** | 36,4% | **78,1%** | +41,7 p.p. |
| **Duplications (%)** | 1,5% | **1,1%** | -0,4 p.p. |

## 3. Justificativa das Métricas que Pioraram em Relação à Fase 02
| Regra SonarQube | Fase 02 | Fase 03 | Saldo | Causa Técnica e Justificativa |
| --- | ---: | ---: | ---: | --- |
| `java:S106` (*Replace `System.out` by a logger*) | 49 | 57 | **+8** | Representa **71,3% (57/80)** de todos os Code Smells do projeto. Como o PokéSal é uma aplicação interativa de terminal compartilhado entre dois jogadores, os novos fluxos de `Main.java` (seleção entre batalha avulsa e torneio de 4/8, escolha de clima, exibição do campeão, tabela de classificação e saldos da `CarteiraPontos`) exigiram novas impressões em `System.out`. Ressalta-se que **todas as novas classes de negócio da Fase 03 (`Torneio`, `ServicoTerrenoClimatico`, `ServicoPosBatalha`, `ResultadoBatalha`, `EventoBatalha`) possuem zero ocorrências de `System.out`**, pois foram desacopladas por interfaces (`RegistroBatalha` e `NotificadorResultado`). |
| `java:S5778` (*Single invocation possibly throwing in `assertThrows` lambda*) | 0 | 4 | **+4** | Aparece nos novos testes parametrizados JUnit 5 (`Fase03BatalhaTest`, `TorneioTest`, `RegrasDominioTest`) em expressões concisas como `assertThrows(IllegalArgumentException.class, () -> new Torneio(participantes(quantidade), ...))`. Optou-se por manter a construção auxiliar inline junto ao construtor testado pela legibilidade direta dos testes parametrizados, pois os métodos auxiliares (`participantes`, `ApoioTeste.golpe`) recebem entradas controladas e quem lança a exceção é comprovadamente o construtor sob teste. |
| `java:S107` (*Constructor has more than 7 parameters*) | 1 | 3 | **+2** | Na Fase 02 apenas `Pokesal` tinha 8 parâmetros; na Fase 03 os dois construtores de `Batalha` passaram a receber 8 e 9 parâmetros (`id`, `treinador1`, `treinador2`, `arena`, `scanner`, `gerador`, `repositorio`, `registro`, `posBatalha`). Esse aumento é consequência direta da **Inversão de Dependência via construtor** exigida na Fase 03 para injetar `GeradorAleatorio`, `RepositorioResultados`, `RegistroBatalha` e `PosBatalha` sem instanciar colaboradores concretos dentro da regra de negócio. |
| `java:S3358` (*Nested ternary operation*) | 0 | 2 | **+2** | Utilizado na formatação compacta da mensagem imutável de `EventoBatalha` em `Batalha.executarAtaque` (diferenciando acerto crítico, acerto normal e erro em uma única expressão sem mutação de variável local). |
| `java:S1144` / `java:S112` / outros pontuais (`S6126`, `S1117`, `S1128`) | 0 | 5 | **+5** | O método privado `Batalha.calcularDano(Pokesal, Pokesal, Golpe)` (`java:S1144`) foi deliberadamente preservado para manter compatibilidade com o teste histórico da Fase 02 (`BatalhaTest.testCalculoDanoBoundaryValues`), que o acessa via reflexão. Já `throws Exception` em `RepositorioResultados.salvar` (`java:S112`) é intencional para desacoplar a interface de domínio de `IOException` específica de arquivos, permitindo testar e isolar qualquer falha de persistência em `Batalha.finalizarBatalha()`. |
| Regras corrigidas na Fase 03 (`S1068`, `S2699`, `S1186`, `S4165`, `S6916`) | 10 | 2 | **-8** | Eliminação do teste vazio sem asserções (`CT-07`), uso efetivo dos IDs das entidades nos registros de torneio/persistência e limpeza do construtor de `Golpe`. |

**Síntese da justificativa:** Embora o total bruto de Code Smells tenha subido de 67 para 80 (+19,4%), o volume de código produtivo e de testes cresceu **+55,4%** (de 901 para 1.400 NCLOC e de 8 para 81 testes), reduzindo a **densidade de Code Smells em 23,2%** (de **74,4** para **57,1 smells/kNCLOC**) e mantendo o **Maintainability Rating A** (`sqale_rating = 1.0`). O aumento absoluto reflete exclusivamente a interface textual expandida em `Main` (`S106`), a injeção explícita de dependências no construtor de `Batalha` (`S107`) e a preservação de compatibilidade com os testes das fases anteriores.

### 3.2 Nota sobre o alerta de Bug mantido (`java:S2184`)

Tanto na Fase 02 quanto na Fase 03 há exatamente **1 alerta classificado como Bug (`MINOR`)**: `java:S2184` em `Batalha.java` (`double danoBase = (ataqueEfetivo + golpe.getPoder()) - defensor.getDefesa();`).

## 4. Evidências e Reprodução Local

### Evidências da Fase 03 (`v3.0.0`)
- [Métricas completas retornadas pela API (`metricas.json`)](evidencias/metricas.json)
- [Status do Quality Gate (`quality-gate.json`)](evidencias/quality-gate.json)
- [Tarefa do Compute Engine (`tarefa.json`)](evidencias/tarefa.json) — ID da análise: `1c8ba4ac-5776-445c-87d9-f4ae9e29c76f`
- [Metadados do envio (`report-task.txt`)](evidencias/report-task.txt)
- [Relatório XML JaCoCo importado (`jacoco.xml`)](evidencias/jacoco.xml)
- [Saída consolidada do Surefire (`testes.txt`)](evidencias/testes.txt)
- [Log completo da execução Maven + SonarScanner (`sonar-scanner.txt`)](evidencias/sonar-scanner.txt)

### Evidências Históricas Preservadas da Fase 02 (`v2.0.0`)
- [Métricas da Fase 02 (`evidencias/fase02/metricas.json`)](evidencias/fase02/metricas.json)
- [Quality Gate da Fase 02 (`evidencias/fase02/quality-gate.json`)](evidencias/fase02/quality-gate.json)
- [Log do Scanner da Fase 02 (`evidencias/fase02/sonar-scanner.txt`)](evidencias/fase02/sonar-scanner.txt)
