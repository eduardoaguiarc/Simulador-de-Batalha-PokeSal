package br.edu.ucsal.pokesal;

import br.edu.ucsal.pokesal.aleatorio.GeradorAleatorio;
import br.edu.ucsal.pokesal.arena.Arena;
import br.edu.ucsal.pokesal.arena.ServicoTerrenoClimatico;
import br.edu.ucsal.pokesal.batalha.Batalha;
import br.edu.ucsal.pokesal.enums.Clima;
import br.edu.ucsal.pokesal.enums.Status;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.enums.TipoTerreno;
import br.edu.ucsal.pokesal.pokesal.Golpe;
import br.edu.ucsal.pokesal.pokesal.Pokesal;
import br.edu.ucsal.pokesal.treinador.Mochila;
import br.edu.ucsal.pokesal.treinador.Treinador;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Scanner;

final class ApoioTeste {
  static Clock relogio(String instante) {
    return Clock.fixed(Instant.parse(instante), ZoneId.of("America/Bahia"));
  }

  static Arena arena(TipoTerreno terreno) {
    Clima clima = terreno == TipoTerreno.POCA_DE_CHUVA ? Clima.CHUVOSO : Clima.ENSOLARADO;
    return new Arena("Teste", terreno, new ServicoTerrenoClimatico(
        relogio("2026-11-02T15:00:00Z"), () -> clima));
  }

  static Golpe golpe(double precisao) {
    return new Golpe("Golpe", 20, precisao, TipoElemental.FOGO, 10, Status.NENHUM, 0);
  }

  static Treinador treinador(int id, int velocidade, Golpe golpe) {
    return new Treinador(id, "Treinador " + id,
        new Pokesal(id, "Pokésal " + id, 100, 50, 50, velocidade,
            TipoElemental.FOGO, List.of(golpe)),
        new Mochila());
  }

  static Batalha batalha(Treinador primeiro, Treinador segundo, Arena arena, Scanner scanner) {
    return new Batalha(1, primeiro, segundo, arena, scanner, () -> 0.5,
        resultado -> {
        }, evento -> {
        });
  }

  static GeradorAleatorio sequencia(double... valores) {
    return new GeradorAleatorio() {
      private int indice;

      @Override
      public double proximoDouble() {
        if (indice == valores.length) {
          throw new AssertionError("Sorteio inesperado: " + indice);
        }
        return valores[indice++];
      }
    };
  }
}
