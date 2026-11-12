param([switch]$Offline)

$ErrorActionPreference = 'Stop'
$raizProjeto = Split-Path -Parent $PSScriptRoot
$maven = Get-Command mvn.cmd -ErrorAction SilentlyContinue

if ($maven) {
  $executavelMaven = $maven.Source
} else {
  # O IntelliJ costuma manter o Maven no cache do wrapper do usuário.
  $cacheWrapper = Join-Path $env:USERPROFILE '.m2/wrapper/dists'
  $instalacao = Get-ChildItem -LiteralPath $cacheWrapper -Recurse -Filter mvn.cmd `
    -ErrorAction SilentlyContinue | Select-Object -First 1
  if (-not $instalacao) {
    throw 'Instale o Maven 3.9+ e adicione sua pasta bin ao PATH.'
  }
  $executavelMaven = $instalacao.FullName
}

# O OneDrive marca diretórios gerados como ReadOnly; o clean do Java pode falhar.
# Remova somente a pasta de saída conhecida, após validar seu caminho absoluto.
$pastaGerada = [System.IO.Path]::GetFullPath(
  (Join-Path $raizProjeto 'Simulador-de-Batalha-PokeSal-main/target'))
if (Test-Path -LiteralPath $pastaGerada) {
  $caminhoResolvido = (Resolve-Path -LiteralPath $pastaGerada).Path
  if ($caminhoResolvido -ne $pastaGerada -or
      -not $caminhoResolvido.StartsWith($raizProjeto + [System.IO.Path]::DirectorySeparatorChar)) {
    throw 'A pasta de saída está fora do caminho esperado do projeto.'
  }
  Remove-Item -LiteralPath $caminhoResolvido -Recurse -Force
}

$argumentos = @(
  '-B',
  "-Dmaven.repo.local=$raizProjeto/.build/m2",
  '-f', "$raizProjeto/Simulador-de-Batalha-PokeSal-main/pom.xml",
  'clean', 'verify'
)
if ($Offline) {
  $argumentos += '-o'
}

& $executavelMaven @argumentos
exit $LASTEXITCODE
