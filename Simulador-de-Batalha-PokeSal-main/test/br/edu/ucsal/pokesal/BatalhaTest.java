package br.edu.ucsal.pokesal;

import br.edu.ucsal.pokesal.arena.Arena;
import br.edu.ucsal.pokesal.batalha.Batalha;
import br.edu.ucsal.pokesal.enums.Status;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.enums.TipoTerreno;
import br.edu.ucsal.pokesal.item.Antidoto;
import br.edu.ucsal.pokesal.item.ItemCura;
import br.edu.ucsal.pokesal.pokesal.Golpe;
import br.edu.ucsal.pokesal.pokesal.Pokesal;
import br.edu.ucsal.pokesal.treinador.Mochila;
import br.edu.ucsal.pokesal.treinador.Treinador;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class BatalhaTest {

    @Test
    public void testOrdemDeAtaquePorVelocidade() {
        Pokesal pokesalRapido = new Pokesal(1, "PokéSal Rápido", 100, 100,
                100, 100, TipoElemental.PLANTA, new ArrayList<>());
        Pokesal pokesalLento = new Pokesal(2, "PokéSal Lento", 100, 100,
                100, 10, TipoElemental.PLANTA, new ArrayList<>());

        Treinador treinadorRapido = new Treinador(1, "Treinador Rápido", pokesalRapido, new Mochila());
        Treinador treinadorLento = new Treinador(2, "Treinador Lento", pokesalLento, new Mochila());

        Batalha batalha = new Batalha(1, treinadorRapido, treinadorLento,
                new Arena("Canteiro", TipoTerreno.CANTEIRO_CENTRAL), new Scanner(System.in));

        assertEquals(treinadorRapido, batalha.determinarPrimeiroAtacante());
    }

    @Test
    public void testUsoLimiteDeItensExcedido() {
        Pokesal pokesal = new Pokesal(1, "PokéSal Teste",
                100, 50, 50, 50, TipoElemental.PLANTA, new ArrayList<>());

        Mochila mochila = new Mochila();

        Antidoto antidoto = new Antidoto(1, "Antidoto");
        ItemCura itemCura1 = new ItemCura(1, "Cura", 20);
        ItemCura itemCura2 = new ItemCura(1, "Cura", 20);

        mochila.adicionarItem(antidoto);
        mochila.adicionarItem(itemCura1);
        mochila.adicionarItem(itemCura2);

        Treinador treinador = new Treinador(1, "Treinador 1", pokesal, mochila);
        Arena arena = new Arena("Asfalto Quente", TipoTerreno.ASFALTO_QUENTE);

        Batalha batalha = new Batalha(1, treinador, treinador, arena, new Scanner(System.in));

        batalha.executarUsoItem(treinador, antidoto);
        batalha.executarUsoItem(treinador, itemCura1);

        assertThrows(IllegalStateException.class, () -> batalha.executarUsoItem(treinador, itemCura2));
    }

    @Test
    public void testCalculoDanoBoundaryValues() throws Exception {
        Arena arena = new Arena("Canteiro", TipoTerreno.CANTEIRO_CENTRAL);
        Pokesal pokesal = new Pokesal(1, "PokeSal", 10, 10, 10, 10, TipoElemental.FOGO, new ArrayList<>());
        Treinador treinador = new Treinador(1, "treinador", pokesal, new Mochila());
        Batalha batalha = new Batalha(1, treinador, treinador, arena, new Scanner(System.in));

        Method calcularDano = Batalha.class.getDeclaredMethod("calcularDano", Pokesal.class, Pokesal.class, Golpe.class);
        calcularDano.setAccessible(true);

        // 1) ATK + poder <= DEF: dano travado no mínimo 1.
        Pokesal atacante = new Pokesal(1, "Atacante", 100, 10, 0, 10, TipoElemental.FOGO, new ArrayList<>());
        Pokesal defensorForte = new Pokesal(1, "Defensor Forte", 100, 0, 100, 10, TipoElemental.FOGO, new ArrayList<>());
        Golpe golpe = new Golpe("Golpe Teste", 5, 1.0, TipoElemental.FOGO, 1, Status.NENHUM, 0.0);

        assertEquals(1, calcularDano.invoke(batalha, atacante, defensorForte, golpe));

        // 2) ATK = DEF = poder = 0: dano travado no mínimo 1.
        Pokesal zerado = new Pokesal(1, "PokeSal Zerado", 100, 0, 0, 10, TipoElemental.FOGO, new ArrayList<>());
        Golpe golpeZerado = new Golpe("Golpe Teste", 0, 1.0, TipoElemental.FOGO, 1, Status.NENHUM, 0.0);

        assertEquals(1, calcularDano.invoke(batalha, zerado, zerado, golpeZerado));

        // 3) Dano não pode deixar o HP abaixo de zero.
        Pokesal defensorFragil = new Pokesal(1, "Defensor Frágil", 1, 0, 0, 10, TipoElemental.FOGO, new ArrayList<>());

        defensorFragil.receberDano(9999);

        assertEquals(0, defensorFragil.getHpAtual());
    }
}
