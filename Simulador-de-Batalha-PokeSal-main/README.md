# Pokésal — Simulador de Batalha

Este é um projeto acadêmico desenvolvido em Java para colocar em prática orientação a objetos, modelagem UML e regras de negócio. A partida acontece pelo console, com os dois jogadores usando o mesmo terminal.

# Link Video Explicativo
https://youtu.be/_2LNdvr_v-I

## Como funciona a partida

Cada treinador informa seu nome e escolhe **um único Pokésal**. Depois, os jogadores escolhem o terreno da arena e a batalha começa. A cada ação, é possível atacar ou usar um item da mochila. A partida termina quando um dos Pokésal fica sem HP.

Os seis iniciais estão divididos em dois grupos:

| Grupo | Planta | Fogo | Água |
| --- | --- | --- | --- |
| 1 | BulbaSal | CharSal | SquirtSal |
| 2 | ChikoSal | CyndaSal | TotoSal |

Cada Pokésal tem HP (vida), ATK (ataque), DEF (defesa), SPD (velocidade) e um tipo elemental.

### Vantagens elementais

O tipo do golpe é comparado com o tipo do defensor para definir o multiplicador de dano:

| Tipo do golpe | Contra Fogo | Contra Água | Contra Planta |
| --- | --- | --- | --- |
| Fogo | ×1,0 | ×0,5 | ×2,0 |
| Água | ×2,0 | ×1,0 | ×0,5 |
| Planta | ×0,5 | ×2,0 | ×1,0 |

### O terreno também conta

| Arena | Efeito |
| --- | --- |
| Asfalto Quente | Golpes de Fogo causam 15% a mais de dano. |
| Poça de Chuva / Piso Escorregadio | Golpes de Água causam 10% a mais de dano. |
| Canteiro Central | Pokésal de Planta recuperam 5% do HP máximo ao final do turno. |

Para a Poça de Chuva, escolhemos implementar o bônus no **dano**, uma das opções previstas no enunciado. A cura respeita o HP máximo e não recupera um Pokésal já derrotado.

### Turnos e efeitos de status

Quem tiver a maior velocidade efetiva age primeiro. Se as velocidades forem iguais, o desempate é aleatório. Se um Pokésal for derrotado durante uma ação, a batalha termina sem esperar o restante do turno.

Os golpes também podem aplicar efeitos de status:

- **Queimado:** reduz o ataque pela metade e causa dano ao final do turno.
- **Envenenado:** causa dano ao final do turno. 
- **Paralisado:** reduz a velocidade pela metade, influenciando a ordem das ações.

Enquanto a batalha continuar, os efeitos de terreno e o dano de status são aplicados ao final de cada turno.

### Mochila

Cada treinador começa com uma Potion, uma Super Potion e um Antidote. As poções recuperam, respectivamente, 20 e 40 pontos de HP; o Antidote remove o status aplicado ao Pokésal.

O limite é de **dois itens por treinador em cada batalha**. Usar um item ocupa a ação daquele treinador no turno, e o item utilizado é retirado da mochila.

## Os três requisitos criados pela equipe

Além das regras propostas para o trabalho, acrescentamos:

1. **Até quatro golpes por Pokésal.** Cada inicial já começa com quatro opções de ataque.
2. **Precisão importa.** Se um golpe falhar no teste de precisão, ele não causa dano.
3. **Golpes têm usos limitados.** Cada golpe possui uma quantidade máxima de utilizações. Uma tentativa consome um uso mesmo quando erra, e golpes esgotados não podem ser escolhidos.

## Organização do código

Os pacotes ficam em `src/br/edu/ucsal/pokesal`:

| Pacote | Responsabilidade |
| --- | --- |
| `app` | Entrada do programa, cadastro dos treinadores e escolhas iniciais. |
| `arena` | Terrenos e seus efeitos na batalha. |
| `batalha` | Ordem dos turnos, ações, cálculo de dano e resultado da partida. |
| `enums` | Tipos elementais, terrenos e status. |
| `item` | Itens de cura e remoção de status. |
| `pokesal` | Atributos dos Pokésal, golpes e efeitos de status. |
| `treinador` | Treinadores e suas mochilas. |

## Diagramas

Estes são os diagramas usados na modelagem do projeto:

### Casos de uso

![Diagrama de casos de uso do simulador](docs/diagramas/diagrama-de-casos-de-uso.png)

### Classes

![Diagrama de classes do Pokésal](docs/diagramas/diagrama-de-classes.png)

## Qualidade do código

### Relatório de testes e rastreabilidade

- [Relatório detalhado dos testes](docs/testes/RELATORIO-TESTES.md): cenários,
  entradas, resultados esperados e observados, cobertura e evidências da execução.
- [Matriz de rastreabilidade](docs/testes/MATRIZ-RASTREABILIDADE.md): relação entre
  requisitos, implementação, testes e lacunas de validação.

Na execução documentada, os oito testes passaram no JUnit; um deles está vazio
e não valida comportamento. O relatório distingue esse caso dos sete testes
com asserções e registra 40,0% de cobertura de linhas pelo JaCoCo.

### SonarQube e cobertura de testes

O [relatório de análise](docs/sonarqube/RELATORIO.md) registra o estado da
execução e as evidências de testes e cobertura. A tentativa de análise do
SonarQube depende de autenticação para concluir a coleta de **Code Smells,
Bugs, Vulnerabilities e Coverage %**. O relatório informa explicitamente
quando uma métrica ainda não está disponível.

Para executar os testes, gerar a cobertura JaCoCo e analisar o projeto:

```powershell
# Requer JDK 21+, sonar-scanner, SonarQube local e SONAR_TOKEN no ambiente.
powershell -ExecutionPolicy Bypass -File scripts/analisar-sonar.ps1
```

Use `-SomenteTestes` para executar apenas testes e cobertura local.

### Práticas adotadas

O código segue estas práticas:

- **Naming Conventions:** métodos e variáveis em `camelCase`, classes e enums em `PascalCase`
  e constantes em `UPPER_CASE`.
- **Javadoc:** documentação nas classes, enums, construtores e métodos públicos, incluindo
  getters, setters e métodos sobrescritos.
- **Valores fixos nomeados:** constantes para dano-base mínimo, poderes e parâmetros dos golpes,
  cura dos itens, multiplicadores de vantagem e efeitos de terreno e status.
- **Formatação:** linhas de até **100 caracteres** nos arquivos Java e recuo de **2 espaços**,
  sem tabulações na indentação.

Os 14 arquivos Java foram revisados com **Checkstyle 14.1.0**, usando **Google Checks**, sem avisos ou erros na verificação.
