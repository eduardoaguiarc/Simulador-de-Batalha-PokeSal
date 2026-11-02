package br.edu.ucsal.pokesal.infraestrutura;

import br.edu.ucsal.pokesal.batalha.EventoBatalha;
import br.edu.ucsal.pokesal.batalha.RegistroBatalha;
import java.io.PrintStream;
import java.util.Objects;

/** Exibe eventos usando uma saída fornecida pela aplicação. */
public final class RegistroBatalhaConsole implements RegistroBatalha {
  private final PrintStream saida;

  /** Recebe a saída do registro. */
  public RegistroBatalhaConsole(PrintStream saida) {
    this.saida = Objects.requireNonNull(saida);
  }

  @Override
  public void registrar(EventoBatalha evento) {
    saida.printf("[Batalha %d | Turno %d | %s] %s%n", evento.idBatalha(),
        evento.turno(), evento.tipo(), evento.descricao());
  }
}
