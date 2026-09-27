# Relatorio SonarQube

Analise executada em 2026-09-27T16:52:19-0300, no SonarQube 26.9.0.129388.
Projeto: Simulador-de-Batalha-PokeSal. Commit de referencia: 01dc18cac1e82ec627d3443ab318a150f1c6f382.
Escopo: todos os arquivos Java em src; testes em test. Sem exclusoes de cobertura.
Foram analisados 14 arquivos Java de producao e cinco classes de testes, incluindo
as alteracoes locais ainda nao commitadas em ArenaTest.java e GolpeTest.java.

| Metrica obrigatoria | Resultado |
| --- | ---: |
| Code Smells | 67 |
| Bugs | 1 |
| Vulnerabilities | 0 |
| Coverage % | 36.4% |

Quality Gate: **OK**.
Testes: 8; falhas: 0; erros: 0.
O CT-07 e um metodo vazio e nao verifica requisitos, conforme a
[matriz de rastreabilidade](../testes/MATRIZ-RASTREABILIDADE.md).
Cobertura de linhas: 40.0%. Cobertura de condicoes: 29.2%.
Coverage combina linhas e condicoes; nao equivale apenas a cobertura de linhas do JaCoCo.

[Painel local do SonarQube](http://localhost:9000/dashboard?id=Simulador-de-Batalha-PokeSal)

## Evidencias

- [Metricas retornadas pela API](evidencias/metricas.json)
- [Processamento da analise](evidencias/tarefa.json), ID: 6d0382db-7703-4d1e-ab2e-298bf7a8de0f
- [Quality Gate](evidencias/quality-gate.json)
- [Identificacao do envio](evidencias/report-task.txt)
- [Cobertura JaCoCo importada](evidencias/jacoco.xml)
- [Execucao dos testes](evidencias/testes.txt)
- [Log do SonarScanner](evidencias/sonar-scanner.txt)

## Reproducao

Requisitos: JDK 21 ou superior, sonar-scanner no PATH, SonarQube local ativo e
SONAR_TOKEN definido no ambiente com permissao de analise e leitura do projeto.
Execute na raiz: powershell -ExecutionPolicy Bypass -File scripts/analisar-sonar.ps1

O script baixa JUnit Platform 1.10.2 e JaCoCo 0.8.14 do Maven Central, compila
com --release 21, executa os testes e importa o XML de cobertura no SonarQube.
O relatorio HTML detalhado fica em out/quality/coverage/index.html.
Consulte a [documentacao do JaCoCo](https://www.jacoco.org/jacoco/trunk/doc/cli.html).
