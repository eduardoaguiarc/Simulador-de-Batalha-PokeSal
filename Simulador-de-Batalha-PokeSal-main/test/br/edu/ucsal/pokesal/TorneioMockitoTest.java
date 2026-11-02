package br.edu.ucsal.pokesal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.ucsal.pokesal.batalha.Batalha;
import br.edu.ucsal.pokesal.resultado.ParticipanteResultado;
import br.edu.ucsal.pokesal.resultado.ResultadoBatalha;
import br.edu.ucsal.pokesal.torneio.FabricaBatalha;
import br.edu.ucsal.pokesal.torneio.Torneio;
import br.edu.ucsal.pokesal.treinador.Treinador;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TorneioMockitoTest {
  @Mock
  private FabricaBatalha fabrica;
  @Mock
  private Batalha semifinal1;
  @Mock
  private Batalha semifinal2;
  @Mock
  private Batalha finalTorneio;

  private ResultadoBatalha configurar(Batalha batalha, int id, Treinador vencedor,
      Treinador perdedor) {
    ResultadoBatalha resultado = new ResultadoBatalha(id, ParticipanteResultado.de(vencedor),
        ParticipanteResultado.de(perdedor), 2);
    when(batalha.getResultado()).thenReturn(resultado);
    when(batalha.obterVencedor()).thenReturn(vencedor);
    return resultado;
  }

  @Test
  void testChaveamentoTorneioAvancaVencedores() {
    Treinador t1 = ApoioTeste.treinador(1, 10, ApoioTeste.golpe(100));
    Treinador t2 = ApoioTeste.treinador(2, 10, ApoioTeste.golpe(100));
    Treinador t3 = ApoioTeste.treinador(3, 10, ApoioTeste.golpe(100));
    Treinador t4 = ApoioTeste.treinador(4, 10, ApoioTeste.golpe(100));
    // Valores exatos tornam uma chave incorreta um erro de teste.
    when(fabrica.criar(1, t1, t2)).thenReturn(semifinal1);
    when(fabrica.criar(2, t3, t4)).thenReturn(semifinal2);
    when(fabrica.criar(3, t2, t3)).thenReturn(finalTorneio);
    ResultadoBatalha r1 = configurar(semifinal1, 1, t2, t1);
    ResultadoBatalha r2 = configurar(semifinal2, 2, t3, t4);
    ResultadoBatalha r3 = configurar(finalTorneio, 3, t3, t2);
    Torneio torneio = new Torneio(List.of(t1, t2, t3, t4), fabrica);
    assertSame(t3, torneio.iniciar());
    assertSame(t3, torneio.getCampeao());
    assertEquals(List.of(r1, r2, r3), torneio.getResultados());
    assertEquals(2, torneio.getRanking().getFirst().vitorias());
    InOrder ordem = inOrder(fabrica, semifinal1, semifinal2, finalTorneio);
    ordem.verify(fabrica).criar(1, t1, t2);
    ordem.verify(semifinal1).iniciarBatalha();
    ordem.verify(fabrica).criar(2, t3, t4);
    ordem.verify(semifinal2).iniciarBatalha();
    ordem.verify(fabrica).criar(3, t2, t3);
    ordem.verify(finalTorneio).iniciarBatalha();
    verifyNoMoreInteractions(fabrica);
  }
}
