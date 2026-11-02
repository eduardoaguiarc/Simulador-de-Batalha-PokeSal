package br.edu.ucsal.pokesal.aleatorio;

/** Fonte injetável de toda a aleatoriedade da batalha. */
@FunctionalInterface
public interface GeradorAleatorio {
  /** Retorna um valor no intervalo [0, 1). */
  double proximoDouble();
}
