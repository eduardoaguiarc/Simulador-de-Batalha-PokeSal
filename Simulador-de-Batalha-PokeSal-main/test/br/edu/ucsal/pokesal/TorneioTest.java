package br.edu.ucsal.pokesal;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.ucsal.pokesal.batalha.Batalha;
import br.edu.ucsal.pokesal.batalha.EventoBatalha;
import br.edu.ucsal.pokesal.batalha.TipoEvento;
import br.edu.ucsal.pokesal.enums.Status;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.enums.TipoTerreno;
import br.edu.ucsal.pokesal.pokesal.Golpe;
import br.edu.ucsal.pokesal.resultado.ResultadoBatalha;
import br.edu.ucsal.pokesal.torneio.Torneio;
import br.edu.ucsal.pokesal.treinador.Treinador;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TorneioTest {
  private List<Treinador> participantes(int quantidade) {
    List<Treinador> treinadores = new ArrayList<>();
    for (int id = 1; id <= quantidade; id++) {
      Golpe golpe = new Golpe("Nocaute", 200, 100, TipoElemental.FOGO, 1, Status.NENHUM, 0);
      treinadores.add(ApoioTeste.treinador(id, 100 - id, golpe));
    }
    return treinadores;
  }

  @ParameterizedTest
  @ValueSource(ints = { 4, 8 })
  void executaChavesComRankingEResultados(int quantidade) {
    List<Treinador> treinadores = participantes(quantidade);
    List<ResultadoBatalha> salvos = new ArrayList<>();
    List<String> confrontos = new ArrayList<>();
    // A preparação da rodada também precisa limpar estado anterior.
    treinadores.getFirst().getPokesal().receberDano(30);
    treinadores.getFirst().getPokesal().aplicarStatus(Status.QUEIMADO);
    Torneio torneio = new Torneio(treinadores, (id, primeiro, segundo) -> {
      for (Treinador treinador : List.of(primeiro, segundo)) {
        assertEquals(100, treinador.getPokesal().getHpAtual());
        assertEquals(Status.NENHUM, treinador.getPokesal().obterStatus());
        assertEquals(1, treinador.getPokesal().getGolpes().getFirst().getUsosRestantes());
      }
      confrontos.add(primeiro.getId() + "x" + segundo.getId());
      return new Batalha(id, primeiro, segundo, ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL),
          new Scanner("1 1"), () -> 0.5, salvos::add, evento -> {
          });
    });
    assertSame(treinadores.getFirst(), torneio.iniciar());
    assertSame(torneio.getCampeao(), torneio.getRanking().getFirst().treinador());
    assertEquals(quantidade - 1, salvos.size());
    assertEquals(salvos, torneio.getResultados());
    assertEquals(quantidade, torneio.getRanking().size());
    assertEquals(quantidade == 4 ? 2 : 3, torneio.getRanking().getFirst().vitorias());
    assertEquals(0, torneio.getRanking().getFirst().derrotas());
    assertEquals(quantidade - 1,
        torneio.getRanking().stream().mapToInt(c -> c.derrotas()).sum());
    assertEquals(quantidade - 1,
        torneio.getRanking().stream().mapToInt(c -> c.vitorias()).sum());
    assertEquals(quantidade == 4 ? List.of("1x2", "3x4", "1x3")
        : List.of("1x2", "3x4", "5x6", "7x8", "1x3", "5x7", "1x5"), confrontos);
    assertThrows(IllegalStateException.class, torneio::iniciar);
    assertThrows(UnsupportedOperationException.class, () -> torneio.getResultados().clear());
  }

  @ParameterizedTest
  @ValueSource(ints = { 4, 8 })
  void falhaEmTodasAsGravacoesNaoInvalidaBatalhasOuRanking(int quantidade) {
    List<EventoBatalha> eventos = new ArrayList<>();
    List<ResultadoBatalha> tentativas = new ArrayList<>();
    Torneio torneio = new Torneio(participantes(quantidade),
        (id, primeiro, segundo) -> new Batalha(id, primeiro, segundo, ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL),
            new Scanner("1 1"), () -> 0.5, resultado -> {
              tentativas.add(resultado);
              throw new IOException("Disco indisponível");
            }, eventos::add));
    assertNotNull(torneio.iniciar());
    assertEquals(quantidade - 1, tentativas.size());
    assertEquals(tentativas, torneio.getResultados());
    List<EventoBatalha> erros = eventos.stream()
        .filter(e -> e.tipo() == TipoEvento.ERRO_REPOSITORIO).toList();
    assertEquals(quantidade - 1, erros.size());
    assertTrue(erros.stream().allMatch(e -> e.descricao().contains("Disco indisponível")));
    assertEquals(quantidade - 1, erros.stream().map(EventoBatalha::idBatalha).distinct().count());
    assertEquals(quantidade == 4 ? 2 : 3, torneio.getRanking().getFirst().vitorias());
  }

  @ParameterizedTest
  @ValueSource(ints = { 0, 1, 2, 3, 5, 7, 9 })
  void rejeitaQuantidadeInvalida(int quantidade) {
    assertThrows(IllegalArgumentException.class,
        () -> new Torneio(participantes(quantidade), (id, a, b) -> null));
  }

  @Test
  void rejeitaTreinadorRepetido() {
    List<Treinador> treinadores = participantes(4);
    treinadores.set(3, treinadores.getFirst());
    assertThrows(IllegalArgumentException.class,
        () -> new Torneio(treinadores, (id, a, b) -> null));
  }
}
