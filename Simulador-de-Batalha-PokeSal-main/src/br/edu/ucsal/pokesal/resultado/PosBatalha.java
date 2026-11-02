package br.edu.ucsal.pokesal.resultado;

/** Ações adicionais executadas uma vez quando a batalha define seu resultado. */
@FunctionalInterface
public interface PosBatalha {
  /** Processa o resultado sem modificar o vencedor ou o placar. */
  void processar(ResultadoBatalha resultado);
}
