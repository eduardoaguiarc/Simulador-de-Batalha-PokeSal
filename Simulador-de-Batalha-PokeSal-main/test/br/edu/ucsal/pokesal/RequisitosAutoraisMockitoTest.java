package br.edu.ucsal.pokesal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.edu.ucsal.pokesal.batalha.EventoBatalha;
import br.edu.ucsal.pokesal.batalha.Batalha;
import br.edu.ucsal.pokesal.batalha.RegistroBatalha;
import br.edu.ucsal.pokesal.batalha.TipoEvento;
import br.edu.ucsal.pokesal.enums.Status;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.enums.TipoTerreno;
import br.edu.ucsal.pokesal.pokesal.Golpe;
import br.edu.ucsal.pokesal.treinador.Treinador;
import java.util.Scanner;
import br.edu.ucsal.pokesal.resultado.CarteiraPontos;
import br.edu.ucsal.pokesal.resultado.NotificadorResultado;
import br.edu.ucsal.pokesal.resultado.ParticipanteResultado;
import br.edu.ucsal.pokesal.resultado.ResultadoBatalha;
import br.edu.ucsal.pokesal.resultado.ServicoPosBatalha;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequisitosAutoraisMockitoTest {
  @Mock
  private CarteiraPontos carteira;
  @Mock
  private NotificadorResultado notificador;
  @Mock
  private RegistroBatalha registro;
  @InjectMocks
  private ServicoPosBatalha servico;

  private ResultadoBatalha resultado(int turnos) {
    return new ResultadoBatalha(9,
        new ParticipanteResultado(1, "Ana", 1, "CharSal"),
        new ParticipanteResultado(2, "Bia", 2, "BulbaSal"), turnos);
  }

  @Test
  void testPremioComBonusAteTresTurnos() {
    servico.processar(resultado(3));
    verify(carteira, times(1)).creditar(1, 150);
    verify(carteira, never()).creditar(eq(2), anyInt());
    verifyNoMoreInteractions(carteira);
  }

  @Test
  void testPremioSemBonusAPartirDeQuatroTurnos() {
    servico.processar(resultado(4));
    verify(carteira).creditar(1, 100);
    verifyNoMoreInteractions(carteira);
  }

  @Test
  void testSemPremioQuandoNenhumTurnoFoiDisputado() {
    servico.processar(resultado(0));
    verify(carteira, never()).creditar(anyInt(), anyInt());
    verify(notificador, times(2)).notificar(anyInt(), any(ResultadoBatalha.class));
  }

  @Test
  void testFalhaNaPremiacaoPreservaResultadoENotificacoes() {
    doThrow(new IllegalStateException("Carteira indisponível"))
        .when(carteira).creditar(1, 150);
    ResultadoBatalha resultado = resultado(1);
    assertDoesNotThrow(() -> servico.processar(resultado));
    assertEquals(1, resultado.vencedor().idTreinador());
    verify(notificador).notificar(1, resultado);
    verify(notificador).notificar(2, resultado);
    verify(registro).registrar(argThat(e -> e.tipo() == TipoEvento.ERRO_PREMIACAO
        && e.idBatalha() == 9 && e.turno() == 1));
  }

  @Test
  void testNotificaVencedorEPerdedorComResultadoCompleto() {
    ResultadoBatalha resultado = resultado(2);
    servico.processar(resultado);
    ArgumentCaptor<ResultadoBatalha> captor = ArgumentCaptor.forClass(ResultadoBatalha.class);
    InOrder ordem = inOrder(notificador);
    // eq fixa o destinatário enquanto capture inspeciona o objeto criado pela
    // regra.
    ordem.verify(notificador).notificar(eq(1), captor.capture());
    ordem.verify(notificador).notificar(eq(2), captor.capture());
    ordem.verifyNoMoreInteractions();
    assertEquals(2, captor.getAllValues().size());
    for (ResultadoBatalha enviado : captor.getAllValues()) {
      assertEquals(resultado, enviado);
      assertEquals("Ana", enviado.vencedor().nomeTreinador());
      assertEquals("Bia", enviado.perdedor().nomeTreinador());
      assertEquals(2, enviado.numeroTurnos());
    }
    verifyNoInteractions(registro);
  }

  @Test
  void testFalhaNoPrimeiroAvisoNaoImpedeAvisoAoPerdedor() {
    doThrow(new IllegalStateException("Canal indisponível"))
        .when(notificador).notificar(eq(1), any(ResultadoBatalha.class));
    ResultadoBatalha resultado = resultado(2);
    assertDoesNotThrow(() -> servico.processar(resultado));
    verify(notificador).notificar(2, resultado);
    verify(carteira).creditar(1, 150);
    ArgumentCaptor<EventoBatalha> erro = ArgumentCaptor.forClass(EventoBatalha.class);
    verify(registro).registrar(erro.capture());
    assertEquals(TipoEvento.ERRO_NOTIFICACAO, erro.getValue().tipo());
    assertTrue(erro.getValue().descricao().contains("Canal indisponível"));
    assertEquals(9, erro.getValue().idBatalha());
    assertEquals(1, resultado.vencedor().idTreinador());
  }

  @Test
  void testFalhaNoSegundoAvisoNaoInvalidaPremiacao() {
    doNothing().when(notificador).notificar(eq(1), any(ResultadoBatalha.class));
    doThrow(new IllegalStateException("Canal indisponível"))
        .when(notificador).notificar(eq(2), any(ResultadoBatalha.class));
    ResultadoBatalha resultado = resultado(4);
    assertDoesNotThrow(() -> servico.processar(resultado));
    verify(notificador).notificar(1, resultado);
    verify(carteira).creditar(1, 100);
    verify(registro).registrar(argThat(e -> e.tipo() == TipoEvento.ERRO_NOTIFICACAO));
  }

  @Test
  void testBatalhaRealPremiaEAvisaUmaUnicaVezMesmoComFalhaNoCsv() {
    Treinador vencedor = ApoioTeste.treinador(1, 20,
        new Golpe("Final", 200, 100, TipoElemental.FOGO, 2, Status.NENHUM, 0));
    Treinador perdedor = ApoioTeste.treinador(2, 10, ApoioTeste.golpe(100));
    Batalha batalha = new Batalha(9, vencedor, perdedor,
        ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL), new Scanner("1 1"), () -> 0.5,
        resultado -> {
          throw new java.io.IOException("Sem disco");
        }, registro, servico);
    assertDoesNotThrow(batalha::iniciarBatalha);
    assertSame(vencedor, batalha.obterVencedor());
    assertThrows(IllegalStateException.class, batalha::iniciarBatalha);
    verify(carteira, times(1)).creditar(1, 150);
    verify(notificador, times(1)).notificar(1, batalha.getResultado());
    verify(notificador, times(1)).notificar(2, batalha.getResultado());
    verifyNoMoreInteractions(carteira, notificador);
  }
}
