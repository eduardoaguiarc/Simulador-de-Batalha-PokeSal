package br.edu.ucsal.pokesal;

import br.edu.ucsal.pokesal.arena.Arena;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.enums.TipoTerreno;
import br.edu.ucsal.pokesal.pokesal.Pokesal;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArenaTest {

    @Test
    public void testEfeitoTerrenoEstacionamentoUCSal() {
        Arena asfalto = new Arena("Asfalto Quente", TipoTerreno.ASFALTO_QUENTE);
        Arena poca = new Arena("Poça de Chuva", TipoTerreno.POCA_DE_CHUVA);
        Arena canteiro = new Arena("Canteiro Central", TipoTerreno.CANTEIRO_CENTRAL);

        assertEquals(1.15, asfalto.calcularMultiplicadorDano(TipoElemental.FOGO));
        assertEquals(1.0, asfalto.calcularMultiplicadorDano(TipoElemental.AGUA));
        assertEquals(1.0, asfalto.calcularMultiplicadorDano(TipoElemental.PLANTA));

        assertEquals(1.10, poca.calcularMultiplicadorDano(TipoElemental.AGUA));
        assertEquals(1.0, poca.calcularMultiplicadorDano(TipoElemental.FOGO));
        assertEquals(1.0, poca.calcularMultiplicadorDano(TipoElemental.PLANTA));

        Pokesal pokesalPlanta = new Pokesal(1, "PokéSal de Planta", 100,
                50, 50, 50, TipoElemental.PLANTA, new ArrayList<>());

        pokesalPlanta.receberDano(20);

        assertEquals(80, pokesalPlanta.getHpAtual());

        canteiro.aplicarEfeitoFimDoTurno(pokesalPlanta);

        assertEquals(85, pokesalPlanta.getHpAtual());
    }
}