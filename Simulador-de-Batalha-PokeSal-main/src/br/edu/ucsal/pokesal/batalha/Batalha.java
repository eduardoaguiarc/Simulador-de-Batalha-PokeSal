package br.edu.ucsal.pokesal.batalha;

import br.edu.ucsal.pokesal.arena.Arena;
import br.edu.ucsal.pokesal.enums.Status;
import br.edu.ucsal.pokesal.item.Item;
import br.edu.ucsal.pokesal.pokesal.Golpe;
import br.edu.ucsal.pokesal.pokesal.Pokesal;
import br.edu.ucsal.pokesal.treinador.Mochila;
import br.edu.ucsal.pokesal.treinador.Treinador;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/** Coordena os turnos, as ações e os efeitos de uma batalha entre dois treinadores. */
public class Batalha {

  /** Quantidade máxima de itens que cada treinador pode usar em uma batalha. */
  public static final int MAX_ITENS_POR_BATALHA = 2;
  private static final int DANO_BASE_MINIMO = 1;
  private static final int FATOR_PORCENTAGEM = 100;
  private static final Random RANDOM = new Random();

  private int id;
  private Treinador treinador1;
  private Treinador treinador2;
  private Arena arena;
  private Scanner scanner;
  private int numeroTurno;
  private int itensUsadosTreinador1;
  private int itensUsadosTreinador2;
  private boolean iniciada;

  /**
   * Prepara uma batalha entre dois treinadores.
   *
   * @param id identificador da batalha
   * @param treinador1 primeiro participante
   * @param treinador2 segundo participante
   * @param arena arena que define os efeitos do terreno
   * @param scanner leitor das escolhas feitas no console
   */
  public Batalha(int id, Treinador treinador1, Treinador treinador2, Arena arena, Scanner scanner) {
    this.id = id;
    this.treinador1 = treinador1;
    this.treinador2 = treinador2;
    this.arena = arena;
    this.scanner = scanner;
    this.numeroTurno = 0;
    this.itensUsadosTreinador1 = 0;
    this.itensUsadosTreinador2 = 0;
    this.iniciada = false;
  }

  /** Executa os turnos até a derrota de um Pokésal e anuncia o resultado. */
  public void iniciarBatalha() {
    iniciada = true;
    numeroTurno = 0;

    treinador1.getPokesal().resetarUsosDosGolpes();
    treinador2.getPokesal().resetarUsosDosGolpes();

    System.out.println("=== BATALHA INICIADA: " + treinador1.getNome()
        + " vs " + treinador2.getNome() + " ===");
    System.out.println("Arena: " + arena.getNome() + " (" + arena.getTipoTerreno() + ")");

    while (!verificarFimDaBatalha()) {
      executarTurno();
    }

    Treinador vencedor = obterVencedor();
    System.out.println("\n=== BATALHA ENCERRADA ===");
    if (vencedor != null) {
      System.out.println("Vencedor: " + vencedor.getNome() + "!");
    } else {
      System.out.println("A batalha terminou em empate.");
    }
  }

  /** Executa as ações na ordem de velocidade e aplica os efeitos se a batalha continuar. */
  public void executarTurno() {
    numeroTurno++;
    System.out.println("\n--- Turno " + numeroTurno + " ---");

    Treinador primeiro = determinarPrimeiroAtacante();
    Treinador segundo = obterAdversario(primeiro);

    realizarAcao(primeiro);

    if (!verificarFimDaBatalha()) {
      realizarAcao(segundo);
    }

    if (!verificarFimDaBatalha()) {
      aplicarEfeitosFimDoTurno();
    }
  }

  /**
   * Escolhe quem age primeiro pela velocidade efetiva, com desempate aleatório.
   *
   * @return treinador que realiza a primeira ação do turno
   */
  public Treinador determinarPrimeiroAtacante() {
    Pokesal p1 = treinador1.getPokesal();
    Pokesal p2 = treinador2.getPokesal();

    int velocidade1 = p1.obterVelocidadeEfetiva();
    int velocidade2 = p2.obterVelocidadeEfetiva();

    if (velocidade1 > velocidade2) {
      return treinador1;
    } else if (velocidade2 > velocidade1) {
      return treinador2;
    } else {
      return RANDOM.nextBoolean() ? treinador1 : treinador2;
    }
  }

  /**
   * Solicita e executa a ação de um treinador cujo Pokésal ainda pode lutar.
   *
   * @param treinador participante que realiza a ação
   */
  public void realizarAcao(Treinador treinador) {
    if (treinador.getPokesal().estaDerrotado()) {
      return;
    }

    exibirPokesal(treinador);

    boolean podeUsarItem = quantidadeItensUsados(treinador) < MAX_ITENS_POR_BATALHA
        && !treinador.getMochila().getItens().isEmpty();

    System.out.println("1 - Atacar");
    if (podeUsarItem) {
      System.out.println("2 - Usar Item");
    } else {
      System.out.println("2 - Usar Item (indisponível)");
    }
    int opcao = lerOpcao(1, 2);

    if (opcao == 1) {
      executarAtaque(treinador);
    } else if (podeUsarItem) {
      Item item = escolherItem(treinador);
      if (item != null) {
        executarUsoItem(treinador, item);
      } else {
        System.out.println(treinador.getNome() + " cancelou o uso de item. Atacando...");
        executarAtaque(treinador);
      }
    } else {
      System.out.println("Ação indisponível. " + treinador.getNome() + " atacará.");
      executarAtaque(treinador);
    }
  }

  /**
   * Solicita a escolha de um golpe com usos restantes.
   *
   * @param treinador participante que escolhe o golpe
   * @return golpe escolhido
   */
  public Golpe escolherGolpe(Treinador treinador) {
    Pokesal pokesal = treinador.getPokesal();
    List<Golpe> golpes = pokesal.getGolpes();

    System.out.println("Escolha o golpe de " + pokesal.getNome() + ":");
    for (int i = 0; i < golpes.size(); i++) {
      Golpe golpe = golpes.get(i);
      System.out.println((i + 1) + " - " + golpe.getNome()
          + " [Poder: " + golpe.getPoder()
          + ", Precisão: " + (int) (golpe.getPrecisao() * FATOR_PORCENTAGEM) + "%"
          + ", Usos: " + golpe.getUsosRestantes() + "/" + golpe.getMaximosUsos() + "]"
          + (golpe.podeUsar() ? "" : " (sem usos restantes)"));
    }

    while (true) {
      int opcao = lerOpcao(1, golpes.size());
      Golpe escolhido = golpes.get(opcao - 1);

      if (escolhido.podeUsar()) {
        return escolhido;
      }

      System.out.println("Esse golpe não possui usos restantes. Escolha outro.");
    }
  }

  /**
   * Solicita um item da mochila se o limite de usos ainda não foi atingido.
   *
   * @param treinador participante que escolhe o item
   * @return item escolhido, ou {@code null} se não houver itens disponíveis para uso
   */
  public Item escolherItem(Treinador treinador) {
    if (quantidadeItensUsados(treinador) >= MAX_ITENS_POR_BATALHA) {
      System.out.println(treinador.getNome() + " já utilizou o máximo de itens nesta batalha.");
      return null;
    }

    Mochila mochila = treinador.getMochila();
    List<Item> itens = mochila.getItens();

    if (itens.isEmpty()) {
      System.out.println(treinador.getNome() + " não possui itens na mochila.");
      return null;
    }

    System.out.println("Escolha o item de " + treinador.getNome() + ":");
    for (int i = 0; i < itens.size(); i++) {
      System.out.println((i + 1) + " - " + itens.get(i).getNome());
    }

    int opcao = lerOpcao(1, itens.size());
    return itens.get(opcao - 1);
  }

  /**
   * Consome um uso do golpe escolhido e, se ele acertar, aplica o dano e o possível status.
   *
   * @param treinador participante que ataca
   */
  public void executarAtaque(Treinador treinador) {
    Treinador adversario = obterAdversario(treinador);
    Golpe golpe = escolherGolpe(treinador);
    Pokesal atacante = treinador.getPokesal();
    Pokesal defensor = adversario.getPokesal();

    golpe.consumirUso();

    if (!golpe.testarAcerto()) {
      System.out.println(atacante.getNome() + " usou " + golpe.getNome() + ", mas o golpe errou!");
      return;
    }

    int dano = calcularDano(atacante, defensor, golpe);
    defensor.receberDano(dano);

    System.out.println(atacante.getNome() + " usou " + golpe.getNome() + " e causou " + dano
        + " de dano em " + defensor.getNome() + "! (HP restante: "
        + Math.max(defensor.getHpAtual(), 0) + "/" + defensor.getHpMaximo() + ")");

    if (!defensor.estaDerrotado()
        && golpe.getStatusAplicado() != null
        && golpe.getStatusAplicado() != Status.NENHUM
        && golpe.testarAplicacaoStatus()) {
      defensor.aplicarStatus(golpe.getStatusAplicado());
      System.out.println(defensor.getNome()
          + " foi afetado por " + golpe.getStatusAplicado() + "!");
    }
  }

  private int calcularDano(Pokesal atacante, Pokesal defensor, Golpe golpe) {
    int ataqueEfetivo = atacante.obterAtaqueEfetivo();
    double multiplicadorTipo =
        golpe.getTipoElemental().calcularMultiplicadorContra(defensor.getTipoElemental());
    double multiplicadorTerreno = arena.calcularMultiplicadorDano(golpe.getTipoElemental());

    double danoBase = (ataqueEfetivo + golpe.getPoder()) - defensor.getDefesa();
    if (danoBase < DANO_BASE_MINIMO) {
      danoBase = DANO_BASE_MINIMO;
    }

    double danoFinal = danoBase * multiplicadorTipo * multiplicadorTerreno;
    return (int) Math.round(danoFinal);
  }

  /**
   * Aplica um item permitido, remove-o da mochila e registra seu uso na batalha.
   *
   * @param treinador participante que usa o item
   * @param item item a aplicar ao Pokésal do treinador
   */
  public void executarUsoItem(Treinador treinador, Item item) {
    if (quantidadeItensUsados(treinador) >= MAX_ITENS_POR_BATALHA) {
      throw new IllegalStateException(treinador.getNome() + " já atingiu o limite de itens por batalha.");
    }

    Pokesal pokesal = treinador.getPokesal();

    if (!item.podeUsar(pokesal)) {
      System.out.println("O item " + item.getNome() + " não pode ser usado agora.");
      return;
    }

    item.aplicarEfeito(pokesal);
    treinador.getMochila().removerItem(item);
    registrarUsoItem(treinador);

    System.out.println(treinador.getNome() + " usou " + item.getNome()
        + " em " + pokesal.getNome() + "!");
  }

  private void registrarUsoItem(Treinador treinador) {
    if (treinador == treinador1) {
      itensUsadosTreinador1++;
    } else if (treinador == treinador2) {
      itensUsadosTreinador2++;
    }
  }

  /** Aplica os efeitos de terreno e de status aos Pokésal dos dois participantes. */
  public void aplicarEfeitosFimDoTurno() {
    aplicarEfeitosDeStatusComTerreno(treinador1.getPokesal());
    aplicarEfeitosDeStatusComTerreno(treinador2.getPokesal());
  }

  private void aplicarEfeitosDeStatusComTerreno(Pokesal pokesal) {
    if (pokesal.estaDerrotado()) {
      return;
    }

    arena.aplicarEfeitoFimDoTurno(pokesal);

    if (!pokesal.estaDerrotado() && pokesal.obterStatus() != Status.NENHUM) {
      pokesal.getEfeitoStatus().aplicarFimDoTurno(pokesal);
    }
  }

  /**
   * Verifica se algum dos Pokésal foi derrotado.
   *
   * @return {@code true} quando a batalha deve terminar
   */
  public boolean verificarFimDaBatalha() {
    return treinador1.getPokesal().estaDerrotado() || treinador2.getPokesal().estaDerrotado();
  }

  /**
   * Obtém o outro participante da batalha.
   *
   * @param treinador participante cujo adversário será consultado
   * @return treinador adversário
   * @throws IllegalArgumentException se o treinador não participa da batalha
   */
  public Treinador obterAdversario(Treinador treinador) {
    if (treinador == treinador1) {
      return treinador2;
    } else if (treinador == treinador2) {
      return treinador1;
    }
    throw new IllegalArgumentException("Treinador não participa desta batalha.");
  }

  /**
   * Consulta quantos itens um participante já utilizou nesta batalha.
   *
   * @param treinador participante consultado
   * @return número de itens utilizados
   * @throws IllegalArgumentException se o treinador não participa da batalha
   */
  public int quantidadeItensUsados(Treinador treinador) {
    if (treinador == treinador1) {
      return itensUsadosTreinador1;
    } else if (treinador == treinador2) {
      return itensUsadosTreinador2;
    }
    throw new IllegalArgumentException("Treinador não participa desta batalha.");
  }

  /**
   * Exibe o nome, o HP e o status do Pokésal de um participante.
   *
   * @param treinador participante cujos dados serão exibidos
   */
  public void exibirPokesal(Treinador treinador) {
    Pokesal pokesal = treinador.getPokesal();
    System.out.println("\n" + treinador.getNome() + " - " + pokesal.getNome()
        + " | HP: " + Math.max(pokesal.getHpAtual(), 0) + "/" + pokesal.getHpMaximo()
        + " | Status: " + pokesal.obterStatus());
  }

  /**
   * Solicita uma opção até receber um inteiro dentro do intervalo permitido.
   *
   * @param minimo menor opção aceita, inclusive
   * @param maximo maior opção aceita, inclusive
   * @return opção válida informada pelo usuário
   */
  public int lerOpcao(int minimo, int maximo) {
    int opcao;
    while (true) {
      System.out.print("Escolha uma opção (" + minimo + "-" + maximo + "): ");
      if (scanner.hasNextInt()) {
        opcao = scanner.nextInt();
        if (opcao >= minimo && opcao <= maximo) {
          return opcao;
        }
      } else {
        scanner.next();
      }
      System.out.println("Opção inválida. Tente novamente.");
    }
  }

  private Treinador obterVencedor() {
    boolean p1Derrotado = treinador1.getPokesal().estaDerrotado();
    boolean p2Derrotado = treinador2.getPokesal().estaDerrotado();

    if (p1Derrotado && !p2Derrotado) {
      return treinador2;
    } else if (p2Derrotado && !p1Derrotado) {
      return treinador1;
    }
    return null;
  }
}
