package br.edu.ucsal.pokesal.app;

import br.edu.ucsal.pokesal.arena.Arena;
import br.edu.ucsal.pokesal.arena.ServicoTerreno;
import br.edu.ucsal.pokesal.arena.ServicoTerrenoClimatico;
import br.edu.ucsal.pokesal.aleatorio.GeradorAleatorio;
import br.edu.ucsal.pokesal.batalha.Batalha;
import br.edu.ucsal.pokesal.batalha.RegistroBatalha;
import br.edu.ucsal.pokesal.enums.Clima;
import br.edu.ucsal.pokesal.enums.Status;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.enums.TipoTerreno;
import br.edu.ucsal.pokesal.item.Antidoto;
import br.edu.ucsal.pokesal.item.ItemCura;
import br.edu.ucsal.pokesal.pokesal.Golpe;
import br.edu.ucsal.pokesal.pokesal.Pokesal;
import br.edu.ucsal.pokesal.treinador.Mochila;
import br.edu.ucsal.pokesal.treinador.Treinador;
import br.edu.ucsal.pokesal.infraestrutura.RegistroBatalhaConsole;
import br.edu.ucsal.pokesal.infraestrutura.RepositorioResultadosCsv;
import br.edu.ucsal.pokesal.infraestrutura.CarteiraPontosMemoria;
import br.edu.ucsal.pokesal.infraestrutura.NotificadorResultadoConsole;
import br.edu.ucsal.pokesal.resultado.ServicoPosBatalha;
import br.edu.ucsal.pokesal.resultado.RepositorioResultados;
import br.edu.ucsal.pokesal.torneio.Classificacao;
import br.edu.ucsal.pokesal.torneio.Torneio;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

/** Ponto de entrada do torneio de Pokésal pelo console. */
public class Main {

  private static final int PODER_INVESTIDA = 20;
  private static final double PRECISAO_INVESTIDA = 100.0;
  private static final int MAX_USOS_INVESTIDA = 20;
  private static final int PODER_ATAQUE_ELEMENTAL = 25;
  private static final double PRECISAO_ATAQUE_ELEMENTAL = 90.0;
  private static final int MAX_USOS_ATAQUE_ELEMENTAL = 15;
  private static final int PODER_INVESTIDA_ELEMENTAL = 30;
  private static final double PRECISAO_INVESTIDA_ELEMENTAL = 75.0;
  private static final int MAX_USOS_INVESTIDA_ELEMENTAL = 10;
  private static final int PODER_GOLPE_STATUS = 15;
  private static final double PRECISAO_GOLPE_STATUS = 85.0;
  private static final int MAX_USOS_GOLPE_STATUS = 10;
  private static final double CHANCE_APLICAR_STATUS = 0.3;
  private static final double SEM_CHANCE_STATUS = 0.0;
  private static final int CURA_POTION = 20;
  private static final int CURA_SUPER_POTION = 40;

  /**
   * Monta as dependências, cadastra os treinadores e executa uma batalha ou
   * torneio.
   *
   */
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    System.out.println("TORNEIO POKÉSAL - Estacionamento UCSal\n");

    System.out.println("1 - Batalha avulsa\n2 - Torneio com 4 treinadores"
        + "\n3 - Torneio com 8 treinadores");
    int modo = lerOpcao(scanner, 1, 3);
    int quantidade = modo == 1 ? 2 : (modo == 2 ? 4 : 8);
    List<Treinador> treinadores = new ArrayList<>();
    for (int id = 1; id <= quantidade; id++) {
      treinadores.add(cadastrarTreinador(scanner, id));
    }

    System.out.println("Clima: 1 - Ensolarado | 2 - Chuvoso | 3 - Nublado");
    Clima clima = Clima.values()[lerOpcao(scanner, 1, 3) - 1];
    Clock relogio = Clock.system(ZoneId.of("America/Bahia"));
    ServicoTerreno servicoTerreno = new ServicoTerrenoClimatico(relogio, () -> clima);
    Arena arena = escolherArena(scanner, servicoTerreno);
    Random random = new Random();
    GeradorAleatorio gerador = random::nextDouble;
    RepositorioResultados repositorio = new RepositorioResultadosCsv(Path.of("resultados.csv"));
    RegistroBatalha registro = new RegistroBatalhaConsole(System.out);
    CarteiraPontosMemoria carteira = new CarteiraPontosMemoria();
    ServicoPosBatalha posBatalha = new ServicoPosBatalha(carteira,
        new NotificadorResultadoConsole(System.out), registro);

    if (modo == 1) {
      new Batalha(1, treinadores.get(0), treinadores.get(1), arena, scanner,
          gerador, repositorio, registro, posBatalha).iniciarBatalha();
    } else {
      Torneio torneio = new Torneio(treinadores,
          (id, primeiro, segundo) -> new Batalha(id, primeiro, segundo, arena, scanner,
              gerador, repositorio, registro, posBatalha));
      Treinador campeao = torneio.iniciar();
      System.out.println("\nCampeão do torneio: " + campeao.getNome());
      exibirRanking(torneio.getRanking());
    }

    System.out.println("\nPontos conquistados nesta sessão:");
    for (Treinador treinador : treinadores) {
      System.out.printf("%s: %d pontos%n", treinador.getNome(), carteira.saldo(treinador.getId()));
    }
    scanner.close();
  }

  private static Treinador cadastrarTreinador(Scanner scanner, int id) {
    System.out.println("--- Cadastro do Treinador " + id + " ---");
    System.out.print("Digite o nome do treinador: ");
    String nome = scanner.next();

    Pokesal inicial = escolherPokesalInicial(scanner, nome);
    Mochila mochila = montarMochila();

    Treinador treinador = new Treinador(id, nome, inicial, mochila);

    System.out.println(nome + " escolheu " + inicial.getNome() + "!\n");
    return treinador;
  }

  private static Pokesal escolherPokesalInicial(Scanner scanner, String nomeTreinador) {
    System.out.println(nomeTreinador + ", escolha o grupo de Pokésal inicial:");
    System.out.println("1 - BulbaSal / CharSal / SquirtSal");
    System.out.println("2 - ChikoSal / CyndaSal / TotoSal");
    int grupo = lerOpcao(scanner, 1, 2);

    System.out.println("Escolha seu Pokésal inicial:");
    Pokesal pokesal;
    if (grupo == 1) {
      System.out.println("1 - BulbaSal (Planta)");
      System.out.println("2 - CharSal (Fogo)");
      System.out.println("3 - SquirtSal (Água)");
      int escolha = lerOpcao(scanner, 1, 3);
      pokesal = switch (escolha) {
        case 1 -> criarPokesal(1, "BulbaSal", 45, 39, 49, 45, TipoElemental.PLANTA);
        case 2 -> criarPokesal(2, "CharSal", 39, 42, 43, 65, TipoElemental.FOGO);
        default -> criarPokesal(3, "SquirtSal", 44, 38, 65, 43, TipoElemental.AGUA);
      };
    } else {
      System.out.println("1 - ChikoSal (Planta)");
      System.out.println("2 - CyndaSal (Fogo)");
      System.out.println("3 - TotoSal (Água)");
      int escolha = lerOpcao(scanner, 1, 3);
      pokesal = switch (escolha) {
        case 1 -> criarPokesal(4, "ChikoSal", 45, 39, 65, 45, TipoElemental.PLANTA);
        case 2 -> criarPokesal(5, "CyndaSal", 39, 42, 43, 65, TipoElemental.FOGO);
        default -> criarPokesal(6, "TotoSal", 50, 52, 64, 43, TipoElemental.AGUA);
      };
    }

    return pokesal;
  }

  private static Pokesal criarPokesal(int id, String nome, int hpMaximo, int ataque,
      int defesa, int velocidade, TipoElemental tipoElemental) {
    List<Golpe> golpes = criarGolpesIniciais(tipoElemental);
    return new Pokesal(id, nome, hpMaximo, ataque, defesa, velocidade, tipoElemental, golpes);
  }

  private static List<Golpe> criarGolpesIniciais(TipoElemental tipo) {
    List<Golpe> golpes = new ArrayList<>();

    golpes.add(new Golpe("Investida", PODER_INVESTIDA, PRECISAO_INVESTIDA, tipo,
        MAX_USOS_INVESTIDA, Status.NENHUM, SEM_CHANCE_STATUS));
    golpes.add(new Golpe("Ataque de " + tipo, PODER_ATAQUE_ELEMENTAL, PRECISAO_ATAQUE_ELEMENTAL,
        tipo, MAX_USOS_ATAQUE_ELEMENTAL, Status.NENHUM, SEM_CHANCE_STATUS));
    golpes.add(criarGolpeDeStatus(tipo));
    golpes.add(new Golpe("Investida de " + tipo, PODER_INVESTIDA_ELEMENTAL,
        PRECISAO_INVESTIDA_ELEMENTAL, tipo, MAX_USOS_INVESTIDA_ELEMENTAL,
        Status.NENHUM, SEM_CHANCE_STATUS));

    return golpes;
  }

  private static Golpe criarGolpeDeStatus(TipoElemental tipo) {
    return switch (tipo) {
      case FOGO -> new Golpe("Chama", PODER_GOLPE_STATUS, PRECISAO_GOLPE_STATUS,
          tipo, MAX_USOS_GOLPE_STATUS, Status.QUEIMADO, CHANCE_APLICAR_STATUS);
      case AGUA -> new Golpe("Onda de Trovão", PODER_GOLPE_STATUS, PRECISAO_GOLPE_STATUS,
          tipo, MAX_USOS_GOLPE_STATUS, Status.PARALISADO, CHANCE_APLICAR_STATUS);
      case PLANTA -> new Golpe("Pó Venenoso", PODER_GOLPE_STATUS, PRECISAO_GOLPE_STATUS,
          tipo, MAX_USOS_GOLPE_STATUS, Status.ENVENENADO, CHANCE_APLICAR_STATUS);
    };

  }

  private static Mochila montarMochila() {
    Mochila mochila = new Mochila();
    mochila.adicionarItem(new ItemCura(1, "Potion", CURA_POTION));
    mochila.adicionarItem(new ItemCura(2, "Super Potion", CURA_SUPER_POTION));
    mochila.adicionarItem(new Antidoto(3, "Antidote"));
    return mochila;
  }

  private static Arena escolherArena(Scanner scanner, ServicoTerreno servicoTerreno) {
    System.out.println("--- Escolha da Arena ---");
    System.out.println("1 - Asfalto Quente (bônus de Fogo com sol, das 06h às 18h)");
    System.out.println("2 - Poça de Chuva / Piso Escorregadio (bônus de Água com chuva)");
    System.out.println("3 - Canteiro Central (cura Pokésal do tipo Planta)");
    int opcao = lerOpcao(scanner, 1, 3);

    TipoTerreno terreno;
    String nomeArena;
    switch (opcao) {
      case 1:
        terreno = TipoTerreno.ASFALTO_QUENTE;
        nomeArena = "Estacionamento - Asfalto Quente";
        break;
      case 2:
        terreno = TipoTerreno.POCA_DE_CHUVA;
        nomeArena = "Estacionamento - Poça de Chuva";
        break;
      default:
        terreno = TipoTerreno.CANTEIRO_CENTRAL;
        nomeArena = "Estacionamento - Canteiro Central";
        break;
    }

    System.out.println();
    return new Arena(nomeArena, terreno, servicoTerreno);
  }

  private static void exibirRanking(List<Classificacao> ranking) {
    System.out.println("\nRanking do torneio:");
    int posicao = 0;
    int vitoriasAnteriores = -1;
    for (int i = 0; i < ranking.size(); i++) {
      Classificacao classificacao = ranking.get(i);
      if (classificacao.vitorias() != vitoriasAnteriores) {
        posicao = i + 1;
        vitoriasAnteriores = classificacao.vitorias();
      }
      System.out.printf("%dº - %s: %d vitória(s), %d derrota(s)%n", posicao,
          classificacao.treinador().getNome(), classificacao.vitorias(), classificacao.derrotas());
    }
  }

  private static int lerOpcao(Scanner scanner, int minimo, int maximo) {
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
}
