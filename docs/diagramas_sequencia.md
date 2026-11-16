# Diagramas de Sequência — Simulador de Batalha PokéSal

Baseados em `Batalha.java`, `Torneio.java` e `ServicoPosBatalha.java`.

---

## Diagrama 1 — Turno completo de batalha (`Batalha.executarTurno()`)

```mermaid
sequenceDiagram
    autonumber
    actor J as Jogador (Scanner/console)
    participant B as Batalha
    participant PA as Pokesal atacante
    participant PD as Pokesal defensor
    participant G as Golpe
    participant GA as GeradorAleatorio
    participant A as Arena
    participant I as Item / Mochila
    participant ES as EfeitoStatus
    participant R as RegistroBatalha

    B->>B: executarTurno()
    Note over B: Falha com IllegalStateException se a batalha já terminou.<br/>executandoTurno = true, numeroTurno++

    B->>B: determinarPrimeiroAtacante()
    B->>PA: obterVelocidadeEfetiva() (dos dois Pokesal)
    PA-->>B: velocidades
    alt velocidades iguais
        B->>GA: proximoDouble()
        GA-->>B: valor (abaixo de 0.5 vence treinador1)
    end
    B->>B: obterAdversario(primeiro)

    loop Ação do primeiro e, depois, do segundo treinador
        B->>B: realizarAcao(treinador)
        opt Pokesal do treinador não derrotado
            B->>J: exibirPokesal() + menu (1 Atacar / 2 Usar Item)
            J-->>B: lerOpcao(1, 2)
            alt Usar Item (limite de 2 não atingido, mochila não vazia, item escolhido)
                B->>J: escolherItem()
                J-->>B: item
                B->>I: podeUsar(pokesal)
                I-->>B: true/false
                opt pode usar
                    B->>I: aplicarEfeito(pokesal)
                    B->>I: removerItem(item)
                    B->>B: itensUsados++ e registrarEvento(USO_ITEM) no buffer
                end
            else Atacar (ou item indisponível/cancelado)
                alt nenhum golpe com usos restantes
                    B->>PA: receberDano(hpAtual)
                    B->>B: registrarEvento(ATAQUE) e registrarEvento(DANO) no buffer
                else há golpe disponível
                    B->>J: escolherGolpe()
                    J-->>B: golpe com usos restantes
                    B->>G: consumirUso()
                    B->>GA: proximoDouble() (acerto vs precisão)
                    opt acertou
                        B->>GA: proximoDouble() (crítico, 6.25%)
                    end
                    B->>B: registrarEvento(ATAQUE) no buffer
                    alt errou
                        B->>B: registrarEvento(DANO, 0) no buffer
                    else acertou
                        B->>A: calcularMultiplicadorDano(tipoDoGolpe)
                        A-->>B: multiplicadorTerreno
                        B->>B: calcularDano()
                        Note right of B: max(1, ataqueEfetivo + poder - defesa)<br/>× multiplicadorTipo × terreno × 1.5 se crítico
                        B->>PD: receberDano(dano)
                        B->>B: registrarEvento(DANO) no buffer
                        opt defensor vivo e golpe aplica status
                            B->>GA: proximoDouble() (vs chanceStatus)
                            opt sorteio favorável
                                B->>PD: aplicarStatus(status)
                                B->>B: registrarEvento(EFEITO_STATUS) no buffer
                            end
                        end
                        opt multiplicadorTerreno diferente de 1.0
                            B->>B: registrarEvento(EFEITO_TERRENO) no buffer
                        end
                    end
                end
            end
        end
        break verificarFimDaBatalha() == true
            Note over B: O segundo treinador não age e os efeitos de fim de turno são pulados
        end
    end

    opt batalha continua
        B->>B: aplicarEfeitosFimDoTurno()
        loop Pokesal do treinador1, depois do treinador2 (se não derrotado)
            B->>A: aplicarEfeitoFimDoTurno(pokesal)
            opt houve cura
                B->>B: registrarEvento(EFEITO_TERRENO) no buffer
            end
            opt status diferente de NENHUM
                B->>ES: aplicarFimDoTurno(pokesal)
                B->>B: registrarEvento(EFEITO_STATUS, hpAntes - hpDepois) no buffer
            end
        end
    end

    Note over B: executandoTurno = false
    B->>B: enviarEventosDoTurno() (ordena por TipoEvento)
    loop cada evento do buffer
        B->>R: registrar(evento)
    end
    opt verificarFimDaBatalha() == true
        B->>B: finalizarBatalha() (ver Diagrama 2)
    end
```

---

## Diagrama 2 — Registro do resultado no torneio (`Torneio.iniciar()`)

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant T as Torneio
    participant PK as Pokesal
    participant F as FabricaBatalha
    participant B as Batalha
    participant RB as ResultadoBatalha
    participant Repo as RepositorioResultados
    participant S as ServicoPosBatalha
    participant CP as CarteiraPontos
    participant N as NotificadorResultado
    participant Reg as RegistroBatalha

    C->>T: iniciar()
    Note over T: IllegalStateException se já iniciado. iniciado = true, idBatalha = 1

    loop enquanto a rodada tiver mais de 1 treinador
        loop para cada par (primeiro, segundo) da rodada
            T->>PK: prepararParaBatalha() (dos dois treinadores)
            T->>F: criar(idBatalha++, primeiro, segundo)
            F-->>T: batalha
            T->>B: iniciarBatalha()
            loop até verificarFimDaBatalha()
                B->>B: executarTurno() (Diagrama 1)
            end

            Note over B: finalizarBatalha() é disparado no fim do último executarTurno().<br/>A chamada seguinte em iniciarBatalha() retorna cedo (resultado já existe).
            B->>B: finalizarBatalha()
            B->>B: obterVencedor()
            alt derrota simultânea (vencedor == null)
                B->>B: sortearTreinador() via GeradorAleatorio
            end
            B->>RB: new ResultadoBatalha(id, vencedor, perdedor, numeroTurno)
            RB-->>B: resultado (imutável)

            B->>Repo: salvar(resultado)
            alt sucesso
                Repo-->>B: ok
            else Exception
                Repo--xB: erro
                B->>Reg: registrar(EventoBatalha ERRO_REPOSITORIO)
                Note over B: O resultado é preservado mesmo sem persistência
            end

            B->>S: processar(resultado)
            opt numeroTurnos maior que 0
                Note over S: pontos = 100 + 50 (bônus se turnos ≤ 3)
                S->>CP: creditar(idVencedor, pontos)
                opt RuntimeException
                    CP--xS: erro
                    S->>Reg: registrar(EventoBatalha ERRO_PREMIACAO)
                end
            end
            loop vencedor, depois perdedor
                S->>N: notificar(idTreinador, resultado)
                opt RuntimeException
                    N--xS: erro
                    S->>Reg: registrar(EventoBatalha ERRO_NOTIFICACAO)
                end
            end
            S-->>B: fim do pós-batalha
            B-->>T: fim de iniciarBatalha()

            T->>B: getResultado()
            B-->>T: resultado
            T->>T: resultados.add(resultado)
            T->>B: obterVencedor()
            B-->>T: vencedor
            T->>T: classificados.add(vencedor)
        end
        T->>T: rodada = classificados
    end

    T->>T: campeao = rodada.getFirst()
    T-->>C: campeao

    opt consulta do ranking
        C->>T: getRanking()
        Note over T: Conta vitórias e derrotas por idTreinador em resultados.<br/>Ordena por vitórias (desc), com sort estável mantendo a ordem de inscrição.
        T-->>C: lista de Classificacao
    end
```

> [!NOTE]
> Falhas em `RepositorioResultados`, `CarteiraPontos` e `ConsoleResultadoTorneio` são isoladas: viram eventos no `RegistroBatalha` e **não** alteram o vencedor nem interrompem o torneio.
