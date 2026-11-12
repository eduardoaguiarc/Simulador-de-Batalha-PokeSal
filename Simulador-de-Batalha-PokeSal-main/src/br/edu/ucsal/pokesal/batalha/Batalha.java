package br.edu.ucsal.pokesal.batalha;

import br.edu.ucsal.pokesal.aleatorio.GeradorAleatorio;
import br.edu.ucsal.pokesal.arena.Arena;
import br.edu.ucsal.pokesal.enums.Status;
import br.edu.ucsal.pokesal.item.Item;
import br.edu.ucsal.pokesal.pokesal.Golpe;
import br.edu.ucsal.pokesal.pokesal.Pokesal;
import br.edu.ucsal.pokesal.resultado.ParticipanteResultado;
import br.edu.ucsal.pokesal.resultado.PosBatalha;
import br.edu.ucsal.pokesal.resultado.RepositorioResultados;
import br.edu.ucsal.pokesal.resultado.ResultadoBatalha;
import br.edu.ucsal.pokesal.treinador.Mochila;
import br.edu.ucsal.pokesal.treinador.Treinador;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/** Coordena os turnos, as ações e os efeitos de uma batalha entre dois treinadores. */
public class Batalha {

  /** Quantidade máxima de itens que cada treinador pode usar em uma batalha. */
  public static final int MAX_ITENS_POR_BATALHA = 2;

  private static final int DANO_BASE_MINIMO = 1;
  private static final int FATOR_PORCENTAGEM = 100;
  private static final double CHANCE_CRITICO = 0.0625;
  private static final double MULTIPLICADOR_CRITICO = 1.5;
  private static final double CHANCE_DESEMPATE = 0.5;
  private final GeradorAleatorio gerador;
  private final RepositorioResultados repositorio;
  private final RegistroBatalha registro;
  private final PosBatalha posBatalha;
  private final List<EventoBatalha> eventosDoTurno = new ArrayList<>();
  private boolean executandoTurno;
  private ResultadoBatalha resultado;
  private Treinador vencedor;

  private final int id;
  private final Treinador treinador1;
  private final Treinador treinador2;
  private final Arena arena;
  private final Scanner scanner;
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
   * @param gerador fonte de sorteios de acerto, crítico, status e desempate
   * @param repositorio destino dos resultados
   * @param registro destino dos eventos e erros
   */
  public Batalha(
      int id,
      Treinador treinador1,
      Treinador treinador2,
      Arena arena,
      Scanner scanner,
      GeradorAleatorio gerador,
      RepositorioResultados repositorio,
      RegistroBatalha registro) {
    this(
        id,
        treinador1,
        treinador2,
        arena,
        scanner,
        gerador,
        repositorio,
        registro,
        resultado -> {});
  }

  /** Prepara uma batalha com as ações autorais de premiação e notificação. */
  public Batalha(
      int id,
      Treinador treinador1,
      Treinador treinador2,
      Arena arena,
      Scanner scanner,
      GeradorAleatorio gerador,
      RepositorioResultados repositorio,
      RegistroBatalha registro,
      PosBatalha posBatalha) {
    if (treinador1 == treinador2) {
      throw new IllegalArgumentException("A batalha exige dois treinadores distintos.");
    }
    this.id = id;
    this.treinador1 = Objects.requireNonNull(treinador1);
    this.treinador2 = Objects.requireNonNull(treinador2);
    this.arena = Objects.requireNonNull(arena);
    this.scanner = Objects.requireNonNull(scanner);
    this.gerador = Objects.requireNonNull(gerador);
    this.repositorio = Objects.requireNonNull(repositorio);
    this.registro = Objects.requireNonNull(registro);
    this.posBatalha = Objects.requireNonNull(posBatalha);
    this.numeroTurno = 0;
    this.itensUsadosTreinador1 = 0;
    this.itensUsadosTreinador2 = 0;
    this.iniciada = false;
  }

  /** Executa os turnos até a derrota de um Pokésal e anuncia o resultado. */
  public void iniciarBatalha() {
    if (iniciada || numeroTurno > 0 || resultado != null) {
      throw new IllegalStateException("Esta batalha já foi iniciada.");
    }
    iniciada = true;
    numeroTurno = 0;

    treinador1.getPokesal().resetarUsosDosGolpes();
    treinador2.getPokesal().resetarUsosDosGolpes();

    System.out.println(
        "=== BATALHA INICIADA: " + treinador1.getNome() + " vs " + treinador2.getNome() + " ===");
    System.out.println("Arena: " + arena.getNome() + " (" + arena.getTipoTerreno() + ")");

    while (!verificarFimDaBatalha()) {
      executarTurno();
    }

    finalizarBatalha();
    System.out.println("\n=== BATALHA ENCERRADA ===");
    System.out.println("Vencedor: " + vencedor.getNome() + "!");
  }

  /** Executa as ações na ordem de velocidade e aplica os efeitos se a batalha continuar. */
  public void executarTurno() {
    if (resultado != null || verificarFimDaBatalha()) {
      throw new IllegalStateException("A batalha já terminou.");
    }
    executandoTurno = true;
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
    executandoTurno = false;
    enviarEventosDoTurno();
    if (verificarFimDaBatalha()) {
      finalizarBatalha();
    }
  }

  private void enviarEventosDoTurno() {
    eventosDoTurno.sort(Comparator.comparing(EventoBatalha::tipo));
    eventosDoTurno.forEach(registro::registrar);
    eventosDoTurno.clear();
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
      return sortearTreinador();
    }
  }

  private Treinador sortearTreinador() {
    return gerador.proximoDouble() < CHANCE_DESEMPATE ? treinador1 : treinador2;
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

    boolean podeUsarItem =
        quantidadeItensUsados(treinador) < MAX_ITENS_POR_BATALHA
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
      return;
    }
    if (!podeUsarItem) {
      System.out.println("Ação indisponível. " + treinador.getNome() + " atacará.");
      executarAtaque(treinador);
      return;
    }

    Item item = escolherItem(treinador);
    if (item == null) {
      System.out.println(treinador.getNome() + " cancelou o uso de item. Atacando...");
      executarAtaque(treinador);
      return;
    }
    executarUsoItem(treinador, item);
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
      System.out.println(
          (i + 1)
              + " - "
              + golpe.getNome()
              + " [Poder: "
              + golpe.getPoder()
              + ", Precisão: "
              + golpe.getPrecisao()
              + "%"
              + ", Usos: "
              + golpe.getUsosRestantes()
              + "/"
              + golpe.getMaximosUsos()
              + "]"
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
    obterAdversario(treinador);
    if (treinador.getPokesal().getGolpes().stream().noneMatch(Golpe::podeUsar)) {
      Pokesal pokesal = treinador.getPokesal();
      int hp = pokesal.getHpAtual();
      pokesal.receberDano(hp);
      registrarEvento(
          TipoEvento.ATAQUE,
          treinador.getNome() + " perdeu por não possuir golpes disponíveis.",
          0,
          false);
      registrarEvento(TipoEvento.DANO, pokesal.getNome() + " ficou sem HP.", hp, false);
      return;
    }
    executarAtaque(treinador, escolherGolpe(treinador));
  }

  /** Executa um golpe escolhido, permitindo entradas diferentes do console e testes. */
  public void executarAtaque(Treinador treinador, Golpe golpe) {
    Treinador adversario = obterAdversario(treinador);
    Pokesal atacante = treinador.getPokesal();
    final Pokesal defensor = adversario.getPokesal();
    if (verificarFimDaBatalha() || !atacante.getGolpes().contains(golpe)) {
      throw new IllegalStateException("Ataque indisponível.");
    }

    golpe.consumirUso();
    boolean acertou = gerador.proximoDouble() < golpe.getPrecisao() / FATOR_PORCENTAGEM;
    boolean critico = acertou && gerador.proximoDouble() < CHANCE_CRITICO;
    String descricao = atacante.getNome() + " usou " + golpe.getNome();
    if (!acertou) {
      descricao += ", mas errou!";
    } else if (critico) {
      descricao += " (crítico).";
    } else {
      descricao += ".";
    }
    registrarEvento(TipoEvento.ATAQUE, descricao, 0, critico);

    if (!acertou) {
      registrarEvento(TipoEvento.DANO, defensor.getNome() + " não recebeu dano.", 0, false);
      System.out.println(atacante.getNome() + " usou " + golpe.getNome() + ", mas o golpe errou!");
      return;
    }

    double multiplicadorTerreno = arena.calcularMultiplicadorDano(golpe.getTipoElemental());
    int dano = calcularDano(atacante, defensor, golpe, multiplicadorTerreno, critico);
    defensor.receberDano(dano);
    registrarEvento(
        TipoEvento.DANO, defensor.getNome() + " recebeu " + dano + " de dano.", dano, critico);

    System.out.println(
        atacante.getNome()
            + " usou "
            + golpe.getNome()
            + " e causou "
            + dano
            + " de dano em "
            + defensor.getNome()
            + "! (HP restante: "
            + Math.max(defensor.getHpAtual(), 0)
            + "/"
            + defensor.getHpMaximo()
            + ")");

    aplicarStatusDoGolpe(defensor, golpe);
    if (multiplicadorTerreno != 1.0) {
      registrarEvento(
          TipoEvento.EFEITO_TERRENO,
          arena.getNome() + ": multiplicador de dano " + multiplicadorTerreno,
          0,
          false);
    }
  }

  private void aplicarStatusDoGolpe(Pokesal defensor, Golpe golpe) {
    if (!defensor.estaDerrotado()
        && golpe.getStatusAplicado() != null
        && golpe.getStatusAplicado() != Status.NENHUM
        && gerador.proximoDouble() < golpe.getChanceStatus()) {
      defensor.aplicarStatus(golpe.getStatusAplicado());
      registrarEvento(
          TipoEvento.EFEITO_STATUS,
          defensor.getNome() + ": " + golpe.getStatusAplicado(),
          0,
          false);
      System.out.println(
          defensor.getNome() + " foi afetado por " + golpe.getStatusAplicado() + "!");
    }
  }

  private int calcularDano(Pokesal atacante, Pokesal defensor, Golpe golpe) {
    return calcularDano(
        atacante,
        defensor,
        golpe,
        arena.calcularMultiplicadorDano(golpe.getTipoElemental()),
        false);
  }

  private int calcularDano(
      Pokesal atacante,
      Pokesal defensor,
      Golpe golpe,
      double multiplicadorTerreno,
      boolean critico) {
    int ataqueEfetivo = atacante.obterAtaqueEfetivo();
    double multiplicadorTipo =
        golpe.getTipoElemental().calcularMultiplicadorContra(defensor.getTipoElemental());

    double danoBase =
        Math.max(
            DANO_BASE_MINIMO, (double) ataqueEfetivo + golpe.getPoder() - defensor.getDefesa());

    double danoFinal =
        danoBase
            * multiplicadorTipo
            * multiplicadorTerreno
            * (critico ? MULTIPLICADOR_CRITICO : 1.0);
    return (int) Math.min(Integer.MAX_VALUE, Math.round(danoFinal));
  }

  /**
   * Aplica um item permitido, remove-o da mochila e registra seu uso na batalha.
   *
   * @param treinador participante que usa o item
   * @param item item a aplicar ao Pokésal do treinador
   */
  public void executarUsoItem(Treinador treinador, Item item) {
    obterAdversario(treinador);
    if (!treinador.getMochila().getItens().contains(item)) {
      throw new IllegalArgumentException("O item não está na mochila deste treinador.");
    }
    if (quantidadeItensUsados(treinador) >= MAX_ITENS_POR_BATALHA) {
      throw new IllegalStateException(
          treinador.getNome() + " já atingiu o limite de itens por batalha.");
    }

    Pokesal pokesal = treinador.getPokesal();

    if (!item.podeUsar(pokesal)) {
      System.out.println("O item " + item.getNome() + " não pode ser usado agora.");
      return;
    }

    item.aplicarEfeito(pokesal);
    treinador.getMochila().removerItem(item);
    registrarUsoItem(treinador);
    registrarEvento(
        TipoEvento.USO_ITEM,
        treinador.getNome() + " usou " + item.getNome() + " em " + pokesal.getNome(),
        0,
        false);

    System.out.println(
        treinador.getNome() + " usou " + item.getNome() + " em " + pokesal.getNome() + "!");
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
    // Preserva a mecânica anterior: terreno antes do status para cada Pokésal.
    // O envio ao RegistroBatalha é ordenado separadamente ao encerrar o turno.
    aplicarEfeitoTerreno(treinador1.getPokesal());
    aplicarEfeitoStatus(treinador1.getPokesal());
    aplicarEfeitoTerreno(treinador2.getPokesal());
    aplicarEfeitoStatus(treinador2.getPokesal());
  }

  private void aplicarEfeitoStatus(Pokesal pokesal) {
    if (!pokesal.estaDerrotado() && pokesal.obterStatus() != Status.NENHUM) {
      int hpAntes = pokesal.getHpAtual();
      Status status = pokesal.obterStatus();
      pokesal.getEfeitoStatus().aplicarFimDoTurno(pokesal);
      registrarEvento(
          TipoEvento.EFEITO_STATUS,
          pokesal.getNome() + ": " + status,
          hpAntes - pokesal.getHpAtual(),
          false);
    }
  }

  private void aplicarEfeitoTerreno(Pokesal pokesal) {
    if (pokesal.estaDerrotado()) {
      return;
    }
    int hpAntes = pokesal.getHpAtual();
    arena.aplicarEfeitoFimDoTurno(pokesal);
    int cura = pokesal.getHpAtual() - hpAntes;
    if (cura > 0) {
      registrarEvento(
          TipoEvento.EFEITO_TERRENO,
          pokesal.getNome() + " recuperou " + cura + " HP no terreno.",
          cura,
          false);
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
    System.out.println(
        "\n"
            + treinador.getNome()
            + " - "
            + pokesal.getNome()
            + " | HP: "
            + Math.max(pokesal.getHpAtual(), 0)
            + "/"
            + pokesal.getHpMaximo()
            + " | Status: "
            + pokesal.obterStatus());
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

  /** Retorna o vencedor conhecido, ou null enquanto não houver resultado decidido. */
  public Treinador obterVencedor() {
    if (vencedor != null) {
      return vencedor;
    }
    boolean p1Derrotado = treinador1.getPokesal().estaDerrotado();
    boolean p2Derrotado = treinador2.getPokesal().estaDerrotado();

    if (p1Derrotado && !p2Derrotado) {
      return treinador2;
    } else if (p2Derrotado && !p1Derrotado) {
      return treinador1;
    }
    return null;
  }

  /** Retorna o resultado imutável, ou null enquanto a batalha não terminou. */
  public ResultadoBatalha getResultado() {
    return resultado;
  }

  private void registrarEvento(TipoEvento tipo, String descricao, int valor, boolean critico) {
    EventoBatalha evento = new EventoBatalha(id, numeroTurno, tipo, descricao, valor, critico);
    if (executandoTurno) {
      eventosDoTurno.add(evento);
    } else {
      registro.registrar(evento);
    }
  }

  private void finalizarBatalha() {
    if (resultado != null) {
      return;
    }
    vencedor = obterVencedor();
    if (vencedor == null) {
      // Derrota simultânea por status: sorteio injetado decide quem avança.
      vencedor = sortearTreinador();
    }
    resultado =
        new ResultadoBatalha(
            id,
            ParticipanteResultado.de(vencedor),
            ParticipanteResultado.de(obterAdversario(vencedor)),
            numeroTurno);
    try {
      repositorio.salvar(resultado);
    } catch (Exception erro) {
      registrarEvento(
          TipoEvento.ERRO_REPOSITORIO,
          "Falha ao salvar resultado: "
              + erro.getClass().getSimpleName()
              + ": "
              + erro.getMessage(),
          0,
          false);
    }
    posBatalha.processar(resultado);
  }
}
