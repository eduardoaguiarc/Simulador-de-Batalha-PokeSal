package br.edu.ucsal.pokesal.infraestrutura;

import br.edu.ucsal.pokesal.resultado.CarteiraPontos;
import java.util.HashMap;
import java.util.Map;

/** Acumula pontos durante a execução atual do programa. */
public final class CarteiraPontosMemoria implements CarteiraPontos {
  private final Map<Integer, Integer> saldos = new HashMap<>();

  @Override
  public void creditar(int idTreinador, int pontos) {
    saldos.merge(idTreinador, pontos, Integer::sum);
  }

  /** Consulta o saldo; treinadores sem vitórias possuem zero pontos. */
  public int saldo(int idTreinador) {
    return saldos.getOrDefault(idTreinador, 0);
  }
}
