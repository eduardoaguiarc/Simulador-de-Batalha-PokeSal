package br.edu.ucsal.pokesal.infraestrutura;

import br.edu.ucsal.pokesal.resultado.NotificadorResultado;
import br.edu.ucsal.pokesal.resultado.ResultadoBatalha;
import java.io.PrintStream;
import java.util.Objects;

/** Entrega os avisos no terminal compartilhado pelos jogadores. */
public final class NotificadorResultadoConsole implements NotificadorResultado {
  private final PrintStream saida;

  /** Recebe o destino de saída dos avisos. */
  public NotificadorResultadoConsole(PrintStream saida) {
    this.saida = Objects.requireNonNull(saida);
  }

  @Override
  public void notificar(int idTreinador, ResultadoBatalha resultado) {
    saida.printf(
        "Aviso ao treinador %d: batalha %d encerrada em %d turno(s). "
            + "Vencedor: %s (%s). Perdedor: %s (%s).%n",
        idTreinador,
        resultado.idBatalha(),
        resultado.numeroTurnos(),
        resultado.vencedor().nomeTreinador(),
        resultado.vencedor().nomePokesal(),
        resultado.perdedor().nomeTreinador(),
        resultado.perdedor().nomePokesal());
  }
}
