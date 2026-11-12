package br.edu.ucsal.pokesal.infraestrutura;

import br.edu.ucsal.pokesal.resultado.ParticipanteResultado;
import br.edu.ucsal.pokesal.resultado.RepositorioResultados;
import br.edu.ucsal.pokesal.resultado.ResultadoBatalha;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/** Acrescenta resultados em CSV UTF-8, preservando as partidas já salvas. */
public final class RepositorioResultadosCsv implements RepositorioResultados {
  private final Path arquivo;

  /** Recebe o caminho de saída escolhido pela aplicação. */
  public RepositorioResultadosCsv(Path arquivo) {
    this.arquivo = Objects.requireNonNull(arquivo);
  }

  @Override
  public void salvar(ResultadoBatalha resultado) throws IOException {
    String cabecalho =
        "batalha,turnos,vencedor_id,vencedor,pokesal_vencedor_id,"
            + "pokesal_vencedor,perdedor_id,perdedor,pokesal_perdedor_id,pokesal_perdedor\n";
    boolean vazio = !Files.exists(arquivo) || Files.size(arquivo) == 0;
    String linha =
        resultado.idBatalha()
            + ","
            + resultado.numeroTurnos()
            + ","
            + participante(resultado.vencedor())
            + ","
            + participante(resultado.perdedor())
            + "\n";
    Files.writeString(
        arquivo,
        (vazio ? cabecalho : "") + linha,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND);
  }

  private String participante(ParticipanteResultado participante) {
    return participante.idTreinador()
        + ","
        + campo(participante.nomeTreinador())
        + ","
        + participante.idPokesal()
        + ","
        + campo(participante.nomePokesal());
  }

  private String campo(String texto) {
    return "\"" + texto.replace("\"", "\"\"") + "\"";
  }
}
