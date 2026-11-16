package br.edu.ucsal.pokesal.infraestrutura;

import br.edu.ucsal.pokesal.resultado.ConsoleResultadoTorneio;
import br.edu.ucsal.pokesal.resultado.ResultadoBatalha;
import java.io.PrintStream;
import java.util.Objects;

/** Entrega os avisos no terminal compartilhado pelos jogadores. */
public final class ResultadoTorneioVencedorConsole implements ConsoleResultadoTorneio {
  private final PrintStream saida;

  /** Recebe o destino de saída dos avisos. */
  public ResultadoTorneioVencedorConsole(PrintStream saida) {
    this.saida = Objects.requireNonNull(saida);
  }

  @Override
  public void notificar(int idTreinador, ResultadoBatalha resultado) {
    saida.printf(
        "Aviso ao treinador %d: %s venceu com %s em %d turno(s). "
            + "(Batalha %d | Perdedor: %s (%s))%n",
        idTreinador,
        resultado.vencedor().nomeTreinador(),
        resultado.vencedor().nomePokesal(),
        resultado.numeroTurnos(),
        resultado.idBatalha(),
        resultado.perdedor().nomeTreinador(),
        resultado.perdedor().nomePokesal());
  }
}
