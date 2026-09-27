# Relatorio SonarQube
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
- 
