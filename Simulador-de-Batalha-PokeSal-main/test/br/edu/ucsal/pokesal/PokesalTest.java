package br.edu.ucsal.pokesal;

import br.edu.ucsal.pokesal.enums.Status;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.pokesal.Golpe;
import br.edu.ucsal.pokesal.pokesal.Pokesal;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PokesalTest {

    @Test
    public void testMaximoDeGolpes() {
        Pokesal pokesal = new Pokesal(1, "Pokesal", 20, 20, 20,
                20, TipoElemental.FOGO, new ArrayList<>());

        Golpe golpe = new Golpe("Golpe 1", 20, 1,
                TipoElemental.FOGO, 20, Status.NENHUM, 0);

        pokesal.adicionarGolpe(golpe);
        pokesal.adicionarGolpe(golpe);
        pokesal.adicionarGolpe(golpe);
        pokesal.adicionarGolpe(golpe);

        assertThrows(IllegalStateException.class, () -> pokesal.adicionarGolpe(golpe));
    }

    @Test
    public void testMaximoDeUsoGolpes() {

    }
}
