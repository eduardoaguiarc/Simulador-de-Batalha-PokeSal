package br.edu.ucsal.pokesal.resultado;

import java.util.List;

/**
 * Resultado definitivo, preservado mesmo se o repositório estiver indisponível.
 */
public record ResultadoBatalha(int idBatalha, ParticipanteResultado vencedor,
    ParticipanteResultado perdedor, int numeroTurnos) {

  /** Lista os dois Pokésal utilizados, na ordem vencedor e perdedor. */
  public List<String> pokesalUtilizados() {
    return List.of(vencedor.nomePokesal(), perdedor.nomePokesal());
  }
}
