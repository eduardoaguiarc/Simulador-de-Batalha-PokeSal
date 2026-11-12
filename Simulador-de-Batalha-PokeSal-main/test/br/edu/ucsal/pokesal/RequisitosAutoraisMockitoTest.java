package br.edu.ucsal.pokesal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import br.edu.ucsal.pokesal.batalha.EventoBatalha;
import br.edu.ucsal.pokesal.batalha.RegistroBatalha;
import br.edu.ucsal.pokesal.batalha.TipoEvento;
import br.edu.ucsal.pokesal.resultado.CarteiraPontos;
import br.edu.ucsal.pokesal.resultado.NotificadorResultado;
import br.edu.ucsal.pokesal.resultado.ParticipanteResultado;
import br.edu.ucsal.pokesal.resultado.ResultadoBatalha;
import br.edu.ucsal.pokesal.resultado.ServicoPosBatalha;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequisitosAutoraisMockitoTest {
  @Mock private CarteiraPontos carteira;
  @Mock private NotificadorResultado notificador;
  @Mock private RegistroBatalha registro;
  @InjectMocks private ServicoPosBatalha servico;

  private ResultadoBatalha resultado(int turnos) {
    return new ResultadoBatalha(
        9,
        new ParticipanteResultado(1, "Ana", 1, "CharSal"),
        new ParticipanteResultado(2, "Bia", 2, "BulbaSal"),
        turnos);
  }

  @Test
  void testPremioComBonusAteTresTurnos() {
    servico.processar(resultado(3));
    verify(carteira, times(1)).creditar(1, 150);
    verify(carteira, never()).creditar(eq(2), anyInt());
    verifyNoMoreInteractions(carteira);
  }

  @Test
  void testPremioSemBonusDepoisDeTresTurnos() {
    servico.processar(resultado(4));
    verify(carteira).creditar(1, 100);
    verifyNoMoreInteractions(carteira);
  }

  @Test
  void testNotificaAmbosComResultadoCompleto() {
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
        .when(notificador)
        .notificar(eq(1), any(ResultadoBatalha.class));
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
}
