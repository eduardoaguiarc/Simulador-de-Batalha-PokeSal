package br.edu.ucsal.pokesal;

import br.edu.ucsal.pokesal.enums.TipoElemental;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TipoElementalTest {

    @Test
    public void testVantagemElemental() {
        // Super efetivo
        assertEquals(2.0, TipoElemental.FOGO.calcularMultiplicadorContra(TipoElemental.PLANTA));
        assertEquals(2.0, TipoElemental.AGUA.calcularMultiplicadorContra(TipoElemental.FOGO));
        assertEquals(2.0, TipoElemental.PLANTA.calcularMultiplicadorContra(TipoElemental.AGUA));

        // Pouco efetivo
        assertEquals(0.5, TipoElemental.FOGO.calcularMultiplicadorContra(TipoElemental.AGUA));
        assertEquals(0.5, TipoElemental.AGUA.calcularMultiplicadorContra(TipoElemental.PLANTA));
        assertEquals(0.5, TipoElemental.PLANTA.calcularMultiplicadorContra(TipoElemental.FOGO));

        // Neutro
        assertEquals(1.0, TipoElemental.FOGO.calcularMultiplicadorContra(TipoElemental.FOGO));
        assertEquals(1.0, TipoElemental.AGUA.calcularMultiplicadorContra(TipoElemental.AGUA));
        assertEquals(1.0, TipoElemental.PLANTA.calcularMultiplicadorContra(TipoElemental.PLANTA));
    }
}
