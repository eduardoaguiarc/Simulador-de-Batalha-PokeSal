package br.edu.ucsal.pokesal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.ucsal.pokesal.aleatorio.GeradorAleatorio;
import br.edu.ucsal.pokesal.arena.Arena;
import br.edu.ucsal.pokesal.batalha.Batalha;
import br.edu.ucsal.pokesal.batalha.EventoBatalha;
import br.edu.ucsal.pokesal.batalha.RegistroBatalha;
import br.edu.ucsal.pokesal.batalha.TipoEvento;
import br.edu.ucsal.pokesal.enums.Status;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.enums.TipoTerreno;
import br.edu.ucsal.pokesal.item.ItemCura;
import br.edu.ucsal.pokesal.pokesal.EfeitoStatus;
import br.edu.ucsal.pokesal.pokesal.Golpe;
import br.edu.ucsal.pokesal.pokesal.Pokesal;
import br.edu.ucsal.pokesal.resultado.ParticipanteResultado;
import br.edu.ucsal.pokesal.resultado.PosBatalha;
import br.edu.ucsal.pokesal.resultado.RepositorioResultados;
import br.edu.ucsal.pokesal.resultado.ResultadoBatalha;
import br.edu.ucsal.pokesal.treinador.Mochila;
import br.edu.ucsal.pokesal.treinador.Treinador;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BatalhaMockitoTest {
  @Mock private GeradorAleatorio gerador;
  @Mock private RepositorioResultados repositorio;
  @Mock private RegistroBatalha registro;
  @Mock private PosBatalha posBatalha;
  private final Treinador primeiro = ApoioTeste.treinador(1, 20, ApoioTeste.golpe(100));
  private final Treinador segundo = ApoioTeste.treinador(2, 10, ApoioTeste.golpe(100));

  private Batalha batalha(String entrada) {
    return new Batalha(
        7,
        primeiro,
        segundo,
        ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL),
        new Scanner(entrada),
        gerador,
        repositorio,
        registro,
        posBatalha);
  }

  private Batalha batalhaComNocaute() {
    primeiro
        .getPokesal()
        .adicionarGolpe(new Golpe("Final", 200, 100, TipoElemental.FOGO, 2, Status.NENHUM, 0));
    when(gerador.proximoDouble()).thenReturn(0.5);
    return batalha("1 2");
  }

  @Test
  void testAcertoCriticoComGeradorMockado() {
    // Primeiro acerto/crítico; depois acerto/limite exclusivo do crítico.
    when(gerador.proximoDouble()).thenReturn(0.5, 0.01, 0.5, 0.0625);

    Batalha batalha = batalha("");
    Golpe golpe = primeiro.getPokesal().getGolpes().getFirst();
    batalha.executarAtaque(primeiro, golpe);

    assertEquals(70, segundo.getPokesal().getHpAtual()); // 20 * 1,5
    batalha.executarAtaque(primeiro, golpe);

    assertEquals(50, segundo.getPokesal().getHpAtual()); // dano normal = 20
    ArgumentCaptor<EventoBatalha> eventos = ArgumentCaptor.forClass(EventoBatalha.class);

    verify(registro, times(4)).registrar(eventos.capture());

    assertEquals(30, eventos.getAllValues().get(1).valor());
    assertTrue(eventos.getAllValues().get(1).critico());
    assertFalse(eventos.getAllValues().get(3).critico());

    verify(gerador, times(4)).proximoDouble();
  }

  @Test
  void testGolpeErraQuandoPrecisaoFalha() {
    Golpe golpe = ApoioTeste.golpe(75);

    primeiro.getPokesal().adicionarGolpe(golpe);

    when(gerador.proximoDouble()).thenReturn(0.75);

    batalha("").executarAtaque(primeiro, golpe);

    assertEquals(100, segundo.getPokesal().getHpAtual());
    assertEquals(9, golpe.getUsosRestantes());

    ArgumentCaptor<EventoBatalha> eventos = ArgumentCaptor.forClass(EventoBatalha.class);

    verify(registro, times(2)).registrar(eventos.capture());

    assertEquals(TipoEvento.DANO, eventos.getValue().tipo());
    assertEquals(0, eventos.getValue().valor());

    verify(gerador, times(1)).proximoDouble();

    for (double precisao : new double[] {-1, 100.01, Double.NaN, Double.POSITIVE_INFINITY}) {
      assertThrows(IllegalArgumentException.class, () -> ApoioTeste.golpe(precisao));
    }
    assertThrows(
        IllegalArgumentException.class,
        () -> new Golpe("Teste", -1, 100, TipoElemental.FOGO, 1, Status.NENHUM, 0));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Golpe("Teste", 10, 100, TipoElemental.FOGO, 0, Status.NENHUM, 0));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Golpe(null, 10, 100, TipoElemental.FOGO, 1, Status.NENHUM, 0));
  }

  @Test
  void testSequenciaDeEventosNoTurno() {
    Golpe veneno = new Golpe("Veneno", 0, 100, TipoElemental.PLANTA, 5, Status.ENVENENADO, 1);
    Treinador planta =
        new Treinador(
            3,
            "Planta",
            new Pokesal(3, "Planta", 100, 50, 50, 10, TipoElemental.PLANTA, List.of(veneno)),
            new Mochila());

    planta.getPokesal().receberDano(20);
    primeiro.getPokesal().receberDano(30);
    primeiro.getMochila().adicionarItem(new ItemCura(1, "Potion", 20));

    when(gerador.proximoDouble()).thenReturn(0.0, 0.5, 0.0);
    Arena arena = ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL);

    Batalha batalha =
        new Batalha(
            7,
            primeiro,
            planta,
            arena,
            new Scanner("2 1 1 1"),
            gerador,
            repositorio,
            registro,
            posBatalha);
    batalha.executarTurno();

    InOrder ordem = inOrder(registro);

    ordem.verify(registro).registrar(argThat(e -> e.tipo() == TipoEvento.ATAQUE));
    ordem.verify(registro).registrar(argThat(e -> e.tipo() == TipoEvento.DANO));
    ordem.verify(registro, times(2)).registrar(argThat(e -> e.tipo() == TipoEvento.EFEITO_STATUS));

    ordem.verify(registro).registrar(argThat(e -> e.tipo() == TipoEvento.EFEITO_TERRENO));
    ordem.verify(registro).registrar(argThat(e -> e.tipo() == TipoEvento.USO_ITEM));
    ordem.verifyNoMoreInteractions();
    verify(registro, atLeastOnce()).registrar(any(EventoBatalha.class));

    assertEquals(85, planta.getPokesal().getHpAtual());
    assertEquals(1, batalha.quantidadeItensUsados(primeiro));

    final Pokesal alvo = ApoioTeste.treinador(4, 10, ApoioTeste.golpe(100)).getPokesal();
    EfeitoStatus queimadura = new EfeitoStatus(Status.QUEIMADO, 1);
    assertEquals(25, queimadura.calcularAtaqueEfetivo(50));
    assertEquals(10, queimadura.calcularVelocidadeEfetiva(10));
    queimadura.aplicarFimDoTurno(alvo);
    assertEquals(93, alvo.getHpAtual());
    assertEquals(Status.NENHUM, queimadura.getTipo());
    queimadura.aplicarFimDoTurno(alvo);
    assertEquals(93, alvo.getHpAtual());

    EfeitoStatus paralisia = new EfeitoStatus(Status.PARALISADO, 0);
    assertEquals(5, paralisia.calcularVelocidadeEfetiva(10));
    assertEquals(50, paralisia.calcularAtaqueEfetivo(50));
    paralisia.aplicarFimDoTurno(alvo);
    assertEquals(93, alvo.getHpAtual());

    EfeitoStatus efeitoVeneno = new EfeitoStatus(Status.ENVENENADO, 0);
    efeitoVeneno.aplicarFimDoTurno(alvo);
    assertEquals(80, alvo.getHpAtual());
    efeitoVeneno.aplicarFimDoTurno(alvo);
    assertEquals(55, alvo.getHpAtual());
    efeitoVeneno.aplicarFimDoTurno(null);
    new EfeitoStatus(null, 0).aplicarFimDoTurno(alvo);
    alvo.receberDano(100);
    efeitoVeneno.aplicarFimDoTurno(alvo);
    assertEquals(0, alvo.getHpAtual());
  }

  @Test
  void testResultadoSalvoAoFimDaBatalha() throws Exception {
    Batalha batalha = batalhaComNocaute();
    batalha.iniciarBatalha();

    ArgumentCaptor<ResultadoBatalha> captor = ArgumentCaptor.forClass(ResultadoBatalha.class);

    verify(repositorio, times(1)).salvar(captor.capture());

    ResultadoBatalha resultado = captor.getValue();

    assertEquals(ParticipanteResultado.de(primeiro), resultado.vencedor());
    assertEquals(ParticipanteResultado.de(segundo), resultado.perdedor());
    assertEquals(1, resultado.numeroTurnos());
    assertEquals(7, resultado.idBatalha());
    assertEquals(List.of("Pokésal 1", "Pokésal 2"), resultado.pokesalUtilizados());
    assertSame(primeiro, batalha.obterVencedor());

    InOrder ordem = inOrder(repositorio, posBatalha);

    ordem.verify(repositorio).salvar(resultado);
    ordem.verify(posBatalha).processar(resultado);

    assertThrows(IllegalStateException.class, batalha::iniciarBatalha);
    assertThrows(IllegalStateException.class, batalha::executarTurno);

    verify(posBatalha, times(1)).processar(resultado);
  }

  @Test
  void testFalhaNoRepositorioNaoInvalidaBatalha() throws Exception {
    doThrow(new IOException("Disco indisponível"))
        .when(repositorio)
        .salvar(any(ResultadoBatalha.class));

    Batalha batalha = batalhaComNocaute();

    assertDoesNotThrow(batalha::iniciarBatalha);
    assertSame(primeiro, batalha.obterVencedor());
    assertEquals(1, batalha.getResultado().vencedor().idTreinador());

    verify(repositorio, times(1)).salvar(batalha.getResultado());
    verify(registro)
        .registrar(
            argThat(
                e ->
                    e.tipo() == TipoEvento.ERRO_REPOSITORIO
                        && e.descricao().contains("Disco indisponível")));
    verify(posBatalha).processar(batalha.getResultado());
  }

  @Test
  void testRepositorioNaoChamadoSeBatalhaNaoTerminou() throws Exception {
    when(gerador.proximoDouble()).thenReturn(0.5);

    Batalha batalha = batalha("1 1 1 1");
    batalha.executarTurno();

    assertFalse(batalha.verificarFimDaBatalha());
    assertNull(batalha.obterVencedor());
    assertNull(batalha.getResultado());

    verify(repositorio, never()).salvar(any(ResultadoBatalha.class));
    verify(posBatalha, never()).processar(any(ResultadoBatalha.class));
  }
}
