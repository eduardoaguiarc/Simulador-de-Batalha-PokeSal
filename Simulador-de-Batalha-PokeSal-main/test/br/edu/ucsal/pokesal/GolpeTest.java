package br.edu.ucsal.pokesal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.edu.ucsal.pokesal.enums.Status;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.pokesal.Golpe;
import br.edu.ucsal.pokesal.pokesal.Pokesal;
import org.junit.jupiter.api.Test;

/** Verifica as regras de negócio da classe. */
public class GolpeTest {

  /** Valida o cenário testMaximoDeUsoGolpes. */
  @Test
  public void testMaximoDeUsoGolpes() {
    Golpe golpe = new Golpe("Golpe Teste", 20, 100, TipoElemental.FOGO, 3, Status.NENHUM, 0);

    golpe.consumirUso();
    golpe.consumirUso();
    golpe.consumirUso();

    // O golpe tem 3 usos disponíveis, e você já consumiu os três
    // O assertThrows tenta consumir o quarto uso e verifica se o código lança uma
    // IllegalStateException, sinalizando que não pode mais usar esse golpe.
    assertThrows(IllegalStateException.class, () -> golpe.consumirUso());

    Pokesal pokesal = ApoioTeste.treinador(1, 10, golpe).getPokesal();
    pokesal.resetarUsosDosGolpes();
    assertEquals(3, golpe.getUsosRestantes());
  }
}
