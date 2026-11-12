package br.edu.ucsal.pokesal.torneio;

import br.edu.ucsal.pokesal.batalha.Batalha;
import br.edu.ucsal.pokesal.treinador.Treinador;

/** Monta batalhas com os colaboradores configurados fora das regras do torneio. */
@FunctionalInterface
public interface FabricaBatalha {
  /** Prepara a batalha entre os participantes de uma chave. */
  Batalha criar(int id, Treinador primeiro, Treinador segundo);
}
