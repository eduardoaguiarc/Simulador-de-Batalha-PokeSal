param([switch]$SomenteTestes)

$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)

function Assert-ExitCode([string]$Etapa) {
    if ($LASTEXITCODE -ne 0) { throw "$Etapa falhou (codigo $LASTEXITCODE)." }
}

New-Item -ItemType Directory -Force out/quality/tools | Out-Null
$downloads = @{
    'junit-console.jar' = 'org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar'
    'jacocoagent.jar' = 'org/jacoco/org.jacoco.agent/0.8.14/org.jacoco.agent-0.8.14-runtime.jar'
    'jacococli.jar' = 'org/jacoco/org.jacoco.cli/0.8.14/org.jacoco.cli-0.8.14-nodeps.jar'
}
foreach ($name in $downloads.Keys) {
    $dest = "out/quality/tools/$name"
    if (!(Test-Path $dest)) {
        Invoke-WebRequest "https://repo.maven.apache.org/maven2/$($downloads[$name])" -OutFile $dest
    }
}

# Limpa somente os artefatos gerados por este script dentro do projeto.
$qualityRoot = [IO.Path]::GetFullPath((Join-Path (Get-Location) 'out/quality'))
foreach ($name in @('classes', 'test-classes', 'junit', 'coverage')) {
    $target = [IO.Path]::GetFullPath((Join-Path $qualityRoot $name))
    if (!$target.StartsWith($qualityRoot + [IO.Path]::DirectorySeparatorChar)) {
        throw 'Diretorio de saida invalido.'
    }
    if (Test-Path -LiteralPath $target) { Remove-Item -LiteralPath $target -Recurse -Force }
    New-Item -ItemType Directory -Path $target | Out-Null
}

$sources = @(Get-ChildItem src -Recurse -Filter '*.java' | ForEach-Object FullName)
& javac --release 21 -encoding UTF-8 -g -d out/quality/classes $sources
Assert-ExitCode 'Compilacao'
$tests = @(Get-ChildItem test -Recurse -Filter '*.java' | ForEach-Object FullName)
& javac --release 21 -encoding UTF-8 -g -cp 'out/quality/classes;out/quality/tools/junit-console.jar' -d out/quality/test-classes $tests
Assert-ExitCode 'Compilacao dos testes'
& java '-javaagent:out/quality/tools/jacocoagent.jar=destfile=out/quality/jacoco.exec,append=false' -jar out/quality/tools/junit-console.jar execute --class-path 'out/quality/classes;out/quality/test-classes' --scan-class-path --reports-dir out/quality/junit --disable-ansi-colors --details=summary *> out/quality/tests.log
Assert-ExitCode 'Testes'
Get-Content out/quality/tests.log
& java -jar out/quality/tools/jacococli.jar report out/quality/jacoco.exec --classfiles out/quality/classes --sourcefiles src --encoding UTF-8 --xml out/quality/jacoco.xml --html out/quality/coverage
Assert-ExitCode 'Relatorio JaCoCo'
if ($SomenteTestes) { return }

if (!$env:SONAR_TOKEN) {
    $env:SONAR_TOKEN = [Environment]::GetEnvironmentVariable('SONAR_TOKEN', 'User')
}
if (!$env:SONAR_TOKEN) { throw 'Configure SONAR_TOKEN para enviar a analise ao SonarQube.' }
$scanner = (Get-Command sonar-scanner -ErrorAction Stop).Source
try {
    $ErrorActionPreference = 'Continue'
    & $scanner '-Dsonar.qualitygate.wait=true' *> out/quality/sonar-scanner.log
    $scannerExit = $LASTEXITCODE
} finally {
    $ErrorActionPreference = 'Stop'
}
Get-Content out/quality/sonar-scanner.log
if (!(Select-String -Path out/quality/sonar-scanner.log -Pattern 'ANALYSIS SUCCESSFUL|EXECUTION SUCCESS|QUALITY GATE STATUS:')) {
    throw "Scanner falhou (codigo $scannerExit). Consulte out/quality/sonar-scanner.log."
}
$taskProperties = ConvertFrom-StringData (Get-Content .scannerwork/report-task.txt -Raw)
$headers = @{ Authorization = 'Bearer ' + $env:SONAR_TOKEN }
$task = Invoke-RestMethod $taskProperties.ceTaskUrl -Headers $headers
if ($task.task.status -ne 'SUCCESS') { throw "Analise ainda nao concluida: $($task.task.status)" }
$server = $taskProperties.serverUrl
$key = [uri]::EscapeDataString($taskProperties.projectKey)
$metricKeys = 'code_smells,bugs,vulnerabilities,coverage,line_coverage,branch_coverage,lines_to_cover,uncovered_lines,conditions_to_cover,uncovered_conditions,tests,test_failures,test_errors,ncloc'
$measures = Invoke-RestMethod "$server/api/measures/component?component=$key&metricKeys=$metricKeys" -Headers $headers
$gate = Invoke-RestMethod "$server/api/qualitygates/project_status?analysisId=$($task.task.analysisId)" -Headers $headers
$evidence = 'docs/sonarqube/evidencias'
New-Item -ItemType Directory -Force $evidence | Out-Null
$task | ConvertTo-Json -Depth 20 | Set-Content "$evidence/tarefa.json" -Encoding UTF8
$measures | ConvertTo-Json -Depth 20 | Set-Content "$evidence/metricas.json" -Encoding UTF8
$gate | ConvertTo-Json -Depth 20 | Set-Content "$evidence/quality-gate.json" -Encoding UTF8
Copy-Item out/quality/jacoco.xml "$evidence/jacoco.xml"
Copy-Item out/quality/tests.log "$evidence/testes.txt"
Copy-Item out/quality/sonar-scanner.log "$evidence/sonar-scanner.txt"
Copy-Item .scannerwork/report-task.txt "$evidence/report-task.txt"
$values = @{}
foreach ($metric in $measures.component.measures) { $values[$metric.metric] = $metric.value }
foreach ($required in @('code_smells', 'bugs', 'vulnerabilities', 'coverage')) {
    if (!$values.ContainsKey($required)) { throw "Metrica ausente: $required" }
}
$revision = git rev-parse HEAD
$report = @"
# Relatorio SonarQube

Analise executada em $($task.task.executedAt), no SonarQube $($taskProperties.serverVersion).
Projeto: Simulador-de-Batalha-PokeSal. Commit de referencia: $revision.
Escopo: todos os arquivos Java em src; testes em test. Sem exclusoes de cobertura.

| Metrica obrigatoria | Resultado |
| --- | ---: |
| Code Smells | $($values['code_smells']) |
| Bugs | $($values['bugs']) |
| Vulnerabilities | $($values['vulnerabilities']) |
| Coverage % | $($values['coverage'])% |

Quality Gate: **$($gate.projectStatus.status)**.
Testes: $($values['tests']); falhas: $($values['test_failures']); erros: $($values['test_errors']).
Cobertura de linhas: $($values['line_coverage'])%. Cobertura de condicoes: $($values['branch_coverage'])%.
Coverage combina linhas e condicoes; nao equivale apenas a cobertura de linhas do JaCoCo.

[Painel local do SonarQube]($($taskProperties.dashboardUrl))

## Evidencias

- [Metricas retornadas pela API](evidencias/metricas.json)
- [Processamento da analise](evidencias/tarefa.json), ID: $($task.task.analysisId)
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
"@
$report | Set-Content docs/sonarqube/RELATORIO.md -Encoding UTF8
Write-Output 'Relatorio salvo em docs/sonarqube/RELATORIO.md'
if ($scannerExit -ne 0) { throw 'Analise processada, mas o scanner reportou falha. Verifique o Quality Gate e o log.' }
