package br.edu.ucsal.pokesal.batalha;

/** Destino injetável para eventos do turno e falhas de persistência. */
@FunctionalInterface
public interface RegistroBatalha {
  /** Recebe um evento, na ordem definida por {@link TipoEvento}. */
  void registrar(EventoBatalha evento);
}
