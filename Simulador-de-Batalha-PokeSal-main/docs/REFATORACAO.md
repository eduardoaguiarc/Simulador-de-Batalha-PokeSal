# Refatoração para facilitar a explicação

## Dependências e execução

O JaCoCo já estava declarado no Maven; faltavam seus arquivos e dependências
transitivas no cache da máquina. O cache `.build/m2` agora permite executar o
projeto sem escrever em diretórios do sistema. O script `scripts/testar.ps1`
localiza o Maven instalado ou o Maven no cache do IntelliJ e executa `clean verify`.
Antes disso, valida o caminho e remove apenas `target` com PowerShell, porque
o OneDrive marca diretórios como somente leitura e pode impedir o clean do Java.
As versões de JUnit, Mockito e JaCoCo ficam centralizadas nas propriedades do POM.
O Checkstyle verifica produção e testes com Google Checks na fase `validate`.

## Classes simplificadas

- `Batalha`: o envio ordenado de eventos e o sorteio de desempate têm métodos
  próprios. A ação do treinador usa retornos antecipados para reduzir blocos
  aninhados. A aplicação de status do golpe foi separada do cálculo de dano.
  O dano-base usa `Math.max`; a conversão antes da soma evita overflow inteiro,
  e o dano final respeita o maior inteiro representável.
  Os colaboradores e participantes fixos foram declarados `final`.
- `Main`: a quantidade de participantes usa um `switch` simples no lugar de
  operadores condicionais aninhados. Continua sendo o ponto de composição.
- `Golpe`: atributos de configuração são `final`; o limite de precisão tem uma
  constante nomeada. Somente a quantidade de usos restantes muda durante a luta.
- `Arena`: nome, terreno e serviço são fixos após a construção.

As interfaces foram preservadas. Pela Inversão de Dependência (DIP, do SOLID),
`Batalha` depende de gerador, registro e repositório abstratos; `Arena` depende de
`ServicoTerreno`, e esse serviço recebe o relógio pelo construtor. Isso reduz o
acoplamento a arquivos, relógio do sistema e aleatoriedade, permitindo mocks.
Métodos menores tornam cada etapa mais legível sem introduzir novas camadas.

## Correção de requisito existente

O veneno causava sempre 12,5% do HP máximo. O enunciado exige dano progressivo:
agora os turnos causam 12,5%, 25%, 37,5% e assim por diante, com arredondamento
e limite de dano igual ao HP máximo. Remover ou reaplicar o status reinicia a
progressão. Não é um requisito autoral novo, mas a correção de uma regra existente.
O teste `BatalhaMockitoTest.testSequenciaDeEventosNoTurno` também verifica a
progressão, a queimadura, a paralisia e a expiração de status.
O teste obrigatório de valores-limite também verifica ATK e poder em
`Integer.MAX_VALUE`, garantindo que o dano continue positivo e limitado.

## Testes

A suíte atual contém o mínimo de **19 métodos de teste** exigidos pelas duas
partes: cinco obrigatórios e dois autorais da parte 1, oito obrigatórios e quatro
autorais da parte 2. O teste duplicado de restauração de usos foi incorporado ao
teste de esgotamento dos golpes. A classe extra `LimitesDominioTest` foi removida:
suas verificações foram integradas aos testes dos respectivos requisitos.
A redução é da quantidade de métodos de teste; as verificações de limites
necessárias à cobertura foram mantidas. Não houve mudança no código de produção.
As metas continuam em 80% de linhas e 70% de branches por classe de negócio,
sem novas exclusões. Os relatórios da suíte de 81 testes são históricos.
