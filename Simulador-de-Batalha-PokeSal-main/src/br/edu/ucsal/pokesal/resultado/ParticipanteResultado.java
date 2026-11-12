package br.edu.ucsal.pokesal.resultado;

import br.edu.ucsal.pokesal.treinador.Treinador;

/** Identifica treinador e Pokésal sem guardar referências ao estado mutável da partida. */
public record ParticipanteResultado(
    int idTreinador, String nomeTreinador, int idPokesal, String nomePokesal) {

  /** Captura a identidade do participante e do Pokésal utilizado. */
  public static ParticipanteResultado de(Treinador treinador) {
    return new ParticipanteResultado(
        treinador.getId(),
        treinador.getNome(),
        treinador.getPokesal().getId(),
        treinador.getPokesal().getNome());
  }
}
