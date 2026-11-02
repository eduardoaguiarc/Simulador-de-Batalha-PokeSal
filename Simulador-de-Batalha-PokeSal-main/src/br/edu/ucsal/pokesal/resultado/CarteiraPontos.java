package br.edu.ucsal.pokesal.resultado;

/** Colaborador externo que acumula os pontos conquistados pelos treinadores. */
@FunctionalInterface
public interface CarteiraPontos {
  /** Credita pontos de uma vitória ao treinador informado. */
  void creditar(int idTreinador, int pontos);
}
