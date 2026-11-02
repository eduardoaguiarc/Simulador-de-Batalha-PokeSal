package br.edu.ucsal.pokesal;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.ucsal.pokesal.aleatorio.GeradorAleatorio;
import br.edu.ucsal.pokesal.arena.Arena;
import br.edu.ucsal.pokesal.batalha.Batalha;
import br.edu.ucsal.pokesal.batalha.EventoBatalha;
import br.edu.ucsal.pokesal.batalha.TipoEvento;
import br.edu.ucsal.pokesal.enums.Status;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.enums.TipoTerreno;
import br.edu.ucsal.pokesal.item.ItemCura;
import br.edu.ucsal.pokesal.pokesal.Golpe;
import br.edu.ucsal.pokesal.pokesal.Pokesal;
import br.edu.ucsal.pokesal.resultado.ResultadoBatalha;
import br.edu.ucsal.pokesal.treinador.Mochila;
import br.edu.ucsal.pokesal.treinador.Treinador;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Fase03BatalhaTest {
  private final List<EventoBatalha> eventos = new ArrayList<>();
  private final Treinador primeiro = ApoioTeste.treinador(1, 20, ApoioTeste.golpe(100));
  private final Treinador segundo = ApoioTeste.treinador(2, 10, ApoioTeste.golpe(100));

  private Batalha batalha(GeradorAleatorio gerador) {
    return new Batalha(1, primeiro, segundo, ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL),
        new Scanner(""), gerador, resultado -> {
        }, eventos::add);
  }

  @ParameterizedTest
  @ValueSource(doubles = { -1, 100.01, Double.NaN, Double.POSITIVE_INFINITY })
  void rejeitaPrecisaoInvalida(double precisao) {
    assertThrows(IllegalArgumentException.class, () -> ApoioTeste.golpe(precisao));
  }

  @Test
  void precisaoZeroErraConsomeUsoSemSortearCriticoOuStatus() {
    Golpe golpe = new Golpe("Falha", 20, 0, TipoElemental.FOGO, 2, Status.QUEIMADO, 1);
    primeiro.getPokesal().adicionarGolpe(golpe);
    batalha(ApoioTeste.sequencia(0)).executarAtaque(primeiro, golpe);
    assertEquals(100, segundo.getPokesal().getHpAtual());
    assertEquals(Status.NENHUM, segundo.getPokesal().obterStatus());
    assertEquals(1, golpe.getUsosRestantes());
    assertEquals(List.of(TipoEvento.ATAQUE, TipoEvento.DANO),
        eventos.stream().map(EventoBatalha::tipo).toList());
    assertEquals(0, eventos.get(1).valor());
  }

  @Test
  void precisaoCemAcertaInclusiveNoMaiorSorteio() {
    batalha(ApoioTeste.sequencia(Math.nextDown(1.0), 0.5))
        .executarAtaque(primeiro, primeiro.getPokesal().getGolpes().getFirst());
    assertEquals(80, segundo.getPokesal().getHpAtual());
  }

  @Test
  void precisaoIntermediariaTemLimiteExclusivo() {
    Golpe golpe = ApoioTeste.golpe(75);
    primeiro.getPokesal().adicionarGolpe(golpe);
    Batalha batalha = batalha(ApoioTeste.sequencia(0.75, Math.nextDown(0.75), 0.5));
    batalha.executarAtaque(primeiro, golpe);
    assertEquals(100, segundo.getPokesal().getHpAtual());
    batalha.executarAtaque(primeiro, golpe);
    assertEquals(80, segundo.getPokesal().getHpAtual());
  }

  @ParameterizedTest
  @ValueSource(doubles = { 0, 0.062499999 })
  void criticoMultiplicaDanoPorUmEMeio(double sorteio) {
    batalha(ApoioTeste.sequencia(0.5, sorteio))
        .executarAtaque(primeiro, primeiro.getPokesal().getGolpes().getFirst());
    assertEquals(70, segundo.getPokesal().getHpAtual());
    assertTrue(eventos.get(0).critico());
    assertEquals(30, eventos.get(1).valor());
  }

  @Test
  void limiteDeSeisEVinteECincoPorCentoNaoECritico() {
    batalha(ApoioTeste.sequencia(0.5, 0.0625))
        .executarAtaque(primeiro, primeiro.getPokesal().getGolpes().getFirst());
    assertEquals(80, segundo.getPokesal().getHpAtual());
    assertFalse(eventos.getFirst().critico());
  }

  @Test
  void combinaCriticoComTipoETerrenoAntesDoArredondamento() {
    Treinador planta = new Treinador(3, "Planta", new Pokesal(3, "Planta", 100,
        50, 50, 10, TipoElemental.PLANTA, List.of(ApoioTeste.golpe(100))), new Mochila());
    Batalha batalha = new Batalha(1, primeiro, planta,
        ApoioTeste.arena(TipoTerreno.ASFALTO_QUENTE), new Scanner(""),
        ApoioTeste.sequencia(0.5, 0), resultado -> {
        }, eventos::add);
    batalha.executarAtaque(primeiro, primeiro.getPokesal().getGolpes().getFirst());
    assertEquals(31, planta.getPokesal().getHpAtual()); // 20 * 2 * 1,15 * 1,5 = 69
    assertEquals(TipoEvento.EFEITO_TERRENO, eventos.getLast().tipo());
  }

  @Test
  void statusUsaMesmoGeradorDepoisDeAcertoECritico() {
    Golpe golpe = new Golpe("Chama", 10, 100, TipoElemental.FOGO, 2, Status.QUEIMADO, 0.3);
    primeiro.getPokesal().adicionarGolpe(golpe);
    Batalha batalha = batalha(ApoioTeste.sequencia(0, 0.5, 0.3, 0, 0.5, 0.2999));
    batalha.executarAtaque(primeiro, golpe);
    assertEquals(Status.NENHUM, segundo.getPokesal().obterStatus());
    batalha.executarAtaque(primeiro, golpe);
    assertEquals(Status.QUEIMADO, segundo.getPokesal().obterStatus());
  }

  @Test
  void desempateDeVelocidadeUsaGeradorInjetado() {
    Treinador empatado = ApoioTeste.treinador(3, 20, ApoioTeste.golpe(100));
    Batalha batalha = new Batalha(1, primeiro, empatado,
        ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL), new Scanner(""),
        ApoioTeste.sequencia(0.4999, 0.5), resultado -> {
        }, eventos::add);
    assertSame(primeiro, batalha.determinarPrimeiroAtacante());
    assertSame(empatado, batalha.determinarPrimeiroAtacante());
  }

  @Test
  void registraTodasAsCategoriasNaOrdemMesmoQuandoItemEPrimeiraAcao() {
    Golpe golpe = new Golpe("Veneno", 0, 100, TipoElemental.PLANTA, 5, Status.ENVENENADO, 1);
    Treinador planta = new Treinador(3, "Planta", new Pokesal(3, "Planta", 100,
        50, 50, 10, TipoElemental.PLANTA, List.of(golpe)), new Mochila());
    planta.getPokesal().receberDano(20);
    primeiro.getPokesal().receberDano(30);
    primeiro.getMochila().adicionarItem(new ItemCura(1, "Potion", 20));
    Batalha batalha = new Batalha(7, primeiro, planta,
        ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL), new Scanner("2 1 1 1"),
        ApoioTeste.sequencia(0, 0.5, 0), resultado -> {
        }, eventos::add);
    batalha.executarTurno();
    assertEquals(List.of(TipoEvento.ATAQUE, TipoEvento.DANO, TipoEvento.EFEITO_STATUS,
        TipoEvento.EFEITO_STATUS, TipoEvento.EFEITO_TERRENO, TipoEvento.USO_ITEM),
        eventos.stream().map(EventoBatalha::tipo).toList());
    assertTrue(eventos.stream().allMatch(e -> e.turno() == 1 && e.idBatalha() == 7));
    assertEquals(85, planta.getPokesal().getHpAtual());
    assertTrue(primeiro.getMochila().getItens().isEmpty());
    assertEquals(1, batalha.quantidadeItensUsados(primeiro));
    assertEquals(0, batalha.quantidadeItensUsados(planta));
  }

  @Test
  void doisAtaquesSaoAgrupadosAntesDosDanos() {
    Batalha batalha = new Batalha(1, primeiro, segundo,
        ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL), new Scanner("1 1 1 1"),
        () -> 0.5, resultado -> {
        }, eventos::add);
    batalha.executarTurno();
    assertEquals(List.of(TipoEvento.ATAQUE, TipoEvento.ATAQUE, TipoEvento.DANO, TipoEvento.DANO),
        eventos.stream().map(EventoBatalha::tipo).toList());
  }

  @Test
  void nocauteInterrompeTurnoEPersisteUmaVezComIdentidadesETurnos() {
    Golpe golpe = new Golpe("Final", 200, 100, TipoElemental.FOGO, 1, Status.QUEIMADO, 1);
    primeiro.getPokesal().adicionarGolpe(golpe);
    List<ResultadoBatalha> salvos = new ArrayList<>();
    Batalha batalha = new Batalha(9, primeiro, segundo,
        ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL), new Scanner("1 2"),
        ApoioTeste.sequencia(0.5, 0.5), salvos::add, eventos::add);
    batalha.iniciarBatalha();
    assertSame(primeiro, batalha.obterVencedor());
    assertEquals(1, salvos.size());
    ResultadoBatalha resultado = salvos.getFirst();
    assertEquals(9, resultado.idBatalha());
    assertEquals(1, resultado.numeroTurnos());
    assertEquals(1, resultado.vencedor().idTreinador());
    assertEquals(2, resultado.perdedor().idTreinador());
    assertEquals(List.of("Pokésal 1", "Pokésal 2"), resultado.pokesalUtilizados());
    assertEquals(List.of(TipoEvento.ATAQUE, TipoEvento.DANO),
        eventos.stream().map(EventoBatalha::tipo).toList());
    assertThrows(IllegalStateException.class, batalha::iniciarBatalha);
    assertThrows(IllegalStateException.class, batalha::executarTurno);
    assertEquals(1, salvos.size());
  }

  @Test
  void naoPermiteUsarItemAusenteDaMochila() {
    primeiro.getPokesal().receberDano(40);
    assertThrows(IllegalArgumentException.class,
        () -> batalha(() -> 0.5).executarUsoItem(primeiro, new ItemCura(1, "Potion", 20)));
    assertEquals(60, primeiro.getPokesal().getHpAtual());
  }

  @Test
  void golpesEsgotadosEncerramBatalhaSemLoopDeSelecao() {
    Golpe golpe = new Golpe("Falha", 20, 0, TipoElemental.FOGO, 1, Status.NENHUM, 0);
    Treinador semAcerto = ApoioTeste.treinador(3, 30, golpe);
    Batalha batalha = new Batalha(1, semAcerto, segundo,
        ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL), new Scanner("1 1 1 1 1"),
        () -> 0.5, resultado -> {
        }, eventos::add);
    batalha.iniciarBatalha();
    assertSame(segundo, batalha.obterVencedor());
    assertEquals(2, batalha.getResultado().numeroTurnos());
  }

  @Test
  void preservaCuraDeTerrenoAntesDoDanoDeStatusDasFasesAnteriores() {
    Treinador planta = new Treinador(3, "Planta", new Pokesal(3, "Planta", 100,
        50, 50, 10, TipoElemental.PLANTA, List.of(ApoioTeste.golpe(100))), new Mochila());
    planta.getPokesal().receberDano(90);
    planta.getPokesal().aplicarStatus(Status.ENVENENADO);
    Batalha batalha = new Batalha(1, primeiro, planta,
        ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL), new Scanner(""),
        () -> 0.5, resultado -> {
        }, eventos::add);
    batalha.aplicarEfeitosFimDoTurno();
    assertEquals(2, planta.getPokesal().getHpAtual()); // 10 + 5 de cura - 13 de veneno
  }

  @Test
  void derrotaSimultaneaPorStatusUsaDesempateInjetadoUmaUnicaVez() {
    primeiro.getPokesal().receberDano(90);
    segundo.getPokesal().receberDano(90);
    primeiro.getPokesal().aplicarStatus(Status.ENVENENADO);
    segundo.getPokesal().aplicarStatus(Status.ENVENENADO);
    primeiro.getPokesal().adicionarGolpe(ApoioTeste.golpe(0));
    segundo.getPokesal().adicionarGolpe(ApoioTeste.golpe(0));
    List<ResultadoBatalha> salvos = new ArrayList<>();
    Batalha batalha = new Batalha(1, primeiro, segundo,
        ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL), new Scanner("1 2 1 2"),
        ApoioTeste.sequencia(0, 0, 0.75), salvos::add, eventos::add);
    batalha.iniciarBatalha();
    assertTrue(primeiro.getPokesal().estaDerrotado());
    assertTrue(segundo.getPokesal().estaDerrotado());
    assertSame(segundo, batalha.obterVencedor());
    assertSame(segundo, batalha.obterVencedor());
    assertEquals(1, salvos.size());
    assertEquals(2, salvos.getFirst().vencedor().idTreinador());
  }
}
