package br.edu.ucsal.pokesal.resultado;

/** Canal externo para entregar o resultado definitivo a cada participante. */
@FunctionalInterface
public interface NotificadorResultado {
  /** Envia o resultado para um dos treinadores participantes. */
  void notificar(int idTreinador, ResultadoBatalha resultado);
}
