package br.edu.ucsal.pokesal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.ucsal.pokesal.enums.Status;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.pokesal.Golpe;
import br.edu.ucsal.pokesal.pokesal.Pokesal;
import org.junit.jupiter.api.Test;

public class GolpeTest {

  //Testa uso máximo de golpes.
  @Test
  public void testMaximoDeUsoGolpes() {
    Golpe golpe = new Golpe("Golpe Teste", 20, 100, TipoElemental.FOGO, 3, Status.NENHUM, 0);

    golpe.consumirUso();
    golpe.consumirUso();
    golpe.consumirUso();

    assertThrows(IllegalStateException.class, () -> golpe.consumirUso());

    Pokesal pokesal = ApoioTeste.treinador(1, 10, golpe).getPokesal();
    pokesal.resetarUsosDosGolpes();
    assertEquals(3, golpe.getUsosRestantes());

    //Testa se o construtor de Golpe rejeita dados fora do contrato lançando.
    for (double precisao : new double[] {-1, 100.01, Double.NaN, Double.POSITIVE_INFINITY}) {
      assertThrows(IllegalArgumentException.class, () -> ApoioTeste.golpe(precisao));
    }
    assertThrows(IllegalArgumentException.class, () -> new Golpe("Teste", -1, 100, TipoElemental.FOGO, 1, Status.NENHUM, 0));
    assertThrows(IllegalArgumentException.class, () -> new Golpe("Teste", 10, 100, TipoElemental.FOGO, 0, Status.NENHUM, 0));
    assertThrows(IllegalArgumentException.class, () -> new Golpe(null, 10, 100, TipoElemental.FOGO, 1, Status.NENHUM, 0));
  }
}
