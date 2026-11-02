package br.edu.ucsal.pokesal.torneio;

import br.edu.ucsal.pokesal.batalha.Batalha;
import br.edu.ucsal.pokesal.resultado.ResultadoBatalha;
import br.edu.ucsal.pokesal.treinador.Treinador;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Torneio eliminatório; participantes são pareados pela ordem de inscrição. */
public final class Torneio {
  private final List<Treinador> participantes;
  private final FabricaBatalha fabrica;
  private final List<ResultadoBatalha> resultados = new ArrayList<>();
  private Treinador campeao;
  private boolean iniciado;

  /**
   * Recebe quatro ou oito treinadores distintos e a fábrica de batalhas injetada.
   */
  public Torneio(List<Treinador> participantes, FabricaBatalha fabrica) {
    Objects.requireNonNull(participantes);
    if (participantes.size() != 4 && participantes.size() != 8) {
      throw new IllegalArgumentException("O torneio exige 4 ou 8 treinadores.");
    }
    Set<Integer> ids = new HashSet<>();
    Set<Object> pokesal = new HashSet<>();
    Set<Object> mochilas = new HashSet<>();
    for (Treinador treinador : participantes) {
      if (treinador == null || treinador.getPokesal() == null || treinador.getMochila() == null
          || !ids.add(treinador.getId()) || !pokesal.add(treinador.getPokesal())
          || !mochilas.add(treinador.getMochila())) {
        throw new IllegalArgumentException("Os treinadores precisam de identidades,"
            + " Pokésal e mochilas próprios.");
      }
    }
    this.participantes = List.copyOf(participantes);
    this.fabrica = Objects.requireNonNull(fabrica);
  }

  /**
   * Executa as rodadas até restar o campeão; cada instância representa um único
   * torneio.
   */
  public Treinador iniciar() {
    if (iniciado) {
      throw new IllegalStateException("Este torneio já foi iniciado.");
    }
    iniciado = true;
    List<Treinador> rodada = participantes;
    int idBatalha = 1;
    while (rodada.size() > 1) {
      List<Treinador> classificados = new ArrayList<>();
      for (int i = 0; i < rodada.size(); i += 2) {
        Treinador primeiro = rodada.get(i);
        Treinador segundo = rodada.get(i + 1);
        primeiro.getPokesal().prepararParaBatalha();
        segundo.getPokesal().prepararParaBatalha();
        Batalha batalha = fabrica.criar(idBatalha++, primeiro, segundo);
        batalha.iniciarBatalha();
        resultados.add(Objects.requireNonNull(batalha.getResultado()));
        classificados.add(Objects.requireNonNull(batalha.obterVencedor()));
      }
      rodada = classificados;
    }
    campeao = rodada.getFirst();
    return campeao;
  }

  /** Retorna o campeão, ou null enquanto o torneio não terminou. */
  public Treinador getCampeao() {
    return campeao;
  }

  /**
   * Retorna os resultados definitivos, inclusive os que falharam ao persistir.
   */
  public List<ResultadoBatalha> getResultados() {
    return List.copyOf(resultados);
  }

  /**
   * Ordena por vitórias; empates mantêm a ordem de inscrição e compartilham
   * posição.
   */
  public List<Classificacao> getRanking() {
    List<Classificacao> ranking = new ArrayList<>();
    for (Treinador treinador : participantes) {
      int vitorias = 0;
      int derrotas = 0;
      for (ResultadoBatalha resultado : resultados) {
        if (resultado.vencedor().idTreinador() == treinador.getId()) {
          vitorias++;
        }
        if (resultado.perdedor().idTreinador() == treinador.getId()) {
          derrotas++;
        }
      }
      ranking.add(new Classificacao(treinador, vitorias, derrotas));
    }
    ranking.sort(Comparator.comparingInt(Classificacao::vitorias).reversed());
    return List.copyOf(ranking);
  }
}
