package br.edu.ucsal.pokesal;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.ucsal.pokesal.infraestrutura.RepositorioResultadosCsv;
import br.edu.ucsal.pokesal.resultado.ParticipanteResultado;
import br.edu.ucsal.pokesal.resultado.ResultadoBatalha;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RepositorioResultadosCsvTest {
  @TempDir
  Path diretorio;

  private ResultadoBatalha resultado(int id) {
    return new ResultadoBatalha(id, new ParticipanteResultado(1, "Ana, \"A\"", 1, "BulbaSal"),
        new ParticipanteResultado(2, "José", 2, "CharSal"), 3);
  }

  @Test
  void persisteSemSobrescreverEProtegeVirgulasEAspas() throws IOException {
    Path arquivo = diretorio.resolve("resultados.csv");
    var repositorio = new RepositorioResultadosCsv(arquivo);
    repositorio.salvar(resultado(1));
    // Uma nova instância precisa preservar os resultados da execução anterior.
    new RepositorioResultadosCsv(arquivo).salvar(resultado(2));
    var linhas = Files.readAllLines(arquivo);
    assertEquals(3, linhas.size());
    assertTrue(linhas.get(1).startsWith("1,3,1,\"Ana, \"\"A\"\"\",1,\"BulbaSal\""));
    assertTrue(linhas.get(2).endsWith("2,\"José\",2,\"CharSal\""));
  }

  @Test
  void propagaErroDeIoParaTratamentoPelaBatalha() {
    var repositorio = new RepositorioResultadosCsv(diretorio);
    assertThrows(IOException.class, () -> repositorio.salvar(resultado(1)));
  }
}
