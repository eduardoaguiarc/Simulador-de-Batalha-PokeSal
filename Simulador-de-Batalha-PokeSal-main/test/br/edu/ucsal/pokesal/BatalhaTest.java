package br.edu.ucsal.pokesal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

/** Verifica as regras de negócio da classe. */
public class BatalhaTest {

  /** Valida o cenário testOrdemDeAtaquePorVelocidade. */
  @Test
  public void testOrdemDeAtaquePorVelocidade() {
    Pokesal pokesalRapido =
        new Pokesal(
            1, "PokéSal Rápido", 100, 100, 100, 100, TipoElemental.PLANTA, new ArrayList<>());
    Pokesal pokesalLento =
        new Pokesal(2, "PokéSal Lento", 100, 100, 100, 10, TipoElemental.PLANTA, new ArrayList<>());

    Treinador treinadorRapido = new Treinador(1, "Treinador Rápido", pokesalRapido, new Mochila());
    Treinador treinadorLento = new Treinador(2, "Treinador Lento", pokesalLento, new Mochila());

    Batalha batalha =
        ApoioTeste.batalha(
            treinadorRapido,
            treinadorLento,
            ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL),
            new Scanner(""));

    assertEquals(treinadorRapido, batalha.determinarPrimeiroAtacante());

    Treinador primeiro = ApoioTeste.treinador(1, 10, ApoioTeste.golpe(100));
    Treinador segundo = ApoioTeste.treinador(2, 10, ApoioTeste.golpe(100));
    Treinador estranho = ApoioTeste.treinador(3, 20, ApoioTeste.golpe(100));
    Batalha batalhaLimite =
        ApoioTeste.batalha(
            primeiro, segundo, ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL), new Scanner(""));
    assertSame(segundo, batalhaLimite.determinarPrimeiroAtacante());
    assertNull(batalhaLimite.escolherItem(primeiro));
    assertThrows(IllegalArgumentException.class, () -> batalhaLimite.obterAdversario(estranho));
    assertThrows(
        IllegalArgumentException.class, () -> batalhaLimite.quantidadeItensUsados(estranho));
    assertThrows(
        IllegalArgumentException.class,
        () -> batalhaLimite.executarUsoItem(primeiro, new ItemCura(1, "Potion", 20)));
    primeiro.getPokesal().receberDano(100);
    batalhaLimite.realizarAcao(primeiro);
    batalhaLimite.aplicarEfeitosFimDoTurno();
    assertEquals(0, primeiro.getPokesal().getHpAtual());
  }

  /** Valida o cenário testUsoLimiteDeItensExcedido. */
  @Test
  public void testUsoLimiteDeItensExcedido() {
    Pokesal pokesal =
        new Pokesal(1, "PokéSal Teste", 100, 50, 50, 50, TipoElemental.PLANTA, new ArrayList<>());

    Mochila mochila = new Mochila();

    Antidoto antidoto = new Antidoto(1, "Antidoto");
    ItemCura itemCura1 = new ItemCura(1, "Cura", 20);
    ItemCura itemCura2 = new ItemCura(1, "Cura", 20);

    mochila.adicionarItem(antidoto);
    mochila.adicionarItem(itemCura1);
    mochila.adicionarItem(itemCura2);

    Treinador treinador = new Treinador(1, "Treinador 1", pokesal, mochila);
    Arena arena = ApoioTeste.arena(TipoTerreno.ASFALTO_QUENTE);

    Batalha batalha =
        ApoioTeste.batalha(
            treinador, ApoioTeste.treinador(2, 10, ApoioTeste.golpe(100)), arena, new Scanner(""));

    batalha.executarUsoItem(treinador, antidoto);
    batalha.executarUsoItem(treinador, itemCura1);

    assertThrows(IllegalStateException.class, () -> batalha.executarUsoItem(treinador, itemCura2));

    final Pokesal alvoLimite = ApoioTeste.treinador(3, 10, ApoioTeste.golpe(100)).getPokesal();
    ItemCura curaLimite = new ItemCura(1, "Potion", 20);
    final Antidoto antidotoLimite = new Antidoto(2, "Antidote");
    Mochila mochilaLimite = new Mochila();
    assertThrows(IllegalArgumentException.class, () -> mochilaLimite.adicionarItem(null));
    mochilaLimite.adicionarItem(curaLimite);
    assertFalse(mochilaLimite.removerItem(null));
    assertFalse(mochilaLimite.removerItem(antidotoLimite));
    assertThrows(UnsupportedOperationException.class, () -> mochilaLimite.getItens().clear());
    assertEquals(List.of(curaLimite), mochilaLimite.getItens());
    alvoLimite.receberDano(100);
    assertFalse(curaLimite.podeUsar(alvoLimite));
    assertFalse(antidotoLimite.podeUsar(alvoLimite));
    curaLimite.aplicarEfeito(alvoLimite);
    antidotoLimite.aplicarEfeito(alvoLimite);
    var arenaLimite = ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL);
    arenaLimite.aplicarEfeitoFimDoTurno(null);
    arenaLimite.aplicarEfeitoFimDoTurno(alvoLimite);
    assertEquals(0, alvoLimite.getHpAtual());
  }

  /** Valida o cenário testCalculoDanoBoundaryValues. */
  @Test
  public void testCalculoDanoBoundaryValues() throws Exception {
    Arena arena = ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL);
    Pokesal pokesal =
        new Pokesal(1, "PokeSal", 10, 10, 10, 10, TipoElemental.FOGO, new ArrayList<>());
    Treinador treinador = new Treinador(1, "treinador", pokesal, new Mochila());
    Batalha batalha =
        ApoioTeste.batalha(
            treinador, ApoioTeste.treinador(2, 10, ApoioTeste.golpe(100)), arena, new Scanner(""));

    Method calcularDano =
        Batalha.class.getDeclaredMethod("calcularDano", Pokesal.class, Pokesal.class, Golpe.class);
    calcularDano.setAccessible(true);

    // 1) ATK + poder <= DEF: dano travado no mínimo 1.
    Pokesal atacante =
        new Pokesal(1, "Atacante", 100, 10, 0, 10, TipoElemental.FOGO, new ArrayList<>());
    Pokesal defensorForte =
        new Pokesal(1, "Defensor Forte", 100, 0, 100, 10, TipoElemental.FOGO, new ArrayList<>());
    Golpe golpe = new Golpe("Golpe Teste", 5, 100, TipoElemental.FOGO, 1, Status.NENHUM, 0.0);

    assertEquals(1, calcularDano.invoke(batalha, atacante, defensorForte, golpe));

    // 2) ATK = DEF = poder = 0: dano travado no mínimo 1.
    Pokesal zerado =
        new Pokesal(1, "PokeSal Zerado", 100, 0, 0, 10, TipoElemental.FOGO, new ArrayList<>());
    Golpe golpeZerado = new Golpe("Golpe Teste", 0, 100, TipoElemental.FOGO, 1, Status.NENHUM, 0.0);

    assertEquals(1, calcularDano.invoke(batalha, zerado, zerado, golpeZerado));

    // 3) Dano não pode deixar o HP abaixo de zero.
    Pokesal defensorFragil =
        new Pokesal(1, "Defensor Frágil", 1, 0, 0, 10, TipoElemental.FOGO, new ArrayList<>());

    defensorFragil.receberDano(9999);

    assertEquals(0, defensorFragil.getHpAtual());

    // 4) Atributos extremos não podem produzir dano negativo por overflow.
    Pokesal atacanteExtremo =
        new Pokesal(
            3, "Extremo", 100, Integer.MAX_VALUE, 0, 10, TipoElemental.FOGO, new ArrayList<>());
    Golpe golpeExtremo =
        new Golpe("Extremo", Integer.MAX_VALUE, 100, TipoElemental.FOGO, 1, Status.NENHUM, 0);
    assertEquals(
        Integer.MAX_VALUE, calcularDano.invoke(batalha, atacanteExtremo, zerado, golpeExtremo));

    for (int[] atributos :
        new int[][] {{0, 10, 10, 10}, {100, -1, 10, 10}, {100, 10, -1, 10}, {100, 10, 10, -1}}) {
      assertThrows(
          IllegalArgumentException.class,
          () ->
              new Pokesal(
                  1,
                  "Teste",
                  atributos[0],
                  atributos[1],
                  atributos[2],
                  atributos[3],
                  TipoElemental.FOGO,
                  List.of()));
    }
    for (String nome : Arrays.asList(null, " ")) {
      assertThrows(
          IllegalArgumentException.class,
          () -> new Pokesal(1, nome, 100, 10, 10, 10, TipoElemental.FOGO, List.of()));
    }
    assertThrows(
        IllegalArgumentException.class,
        () -> new Pokesal(1, "Teste", 100, 10, 10, 10, null, List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Pokesal(1, "Teste", 100, 10, 10, 10, TipoElemental.FOGO, null));

    final Pokesal alvo = ApoioTeste.treinador(3, 10, ApoioTeste.golpe(100)).getPokesal();
    assertThrows(IllegalArgumentException.class, () -> alvo.receberDano(-1));
    assertThrows(IllegalArgumentException.class, () -> alvo.recuperarHp(-1));
    assertThrows(IllegalArgumentException.class, () -> alvo.adicionarGolpe(null));
    assertThrows(IllegalArgumentException.class, () -> alvo.aplicarStatus(null));
    assertEquals(100, alvo.getHpAtual());
    alvo.aplicarStatus(Status.QUEIMADO);
    assertEquals(25, alvo.obterAtaqueEfetivo());
    alvo.aplicarStatus(Status.NENHUM);
    assertEquals(Status.NENHUM, alvo.obterStatus());
    assertEquals(50, alvo.obterAtaqueEfetivo());
    assertThrows(UnsupportedOperationException.class, () -> alvo.getGolpes().clear());
  }
}
