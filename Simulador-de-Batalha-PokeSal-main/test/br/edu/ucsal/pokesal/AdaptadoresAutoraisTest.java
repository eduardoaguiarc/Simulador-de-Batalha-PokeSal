package br.edu.ucsal.pokesal;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.ucsal.pokesal.infraestrutura.CarteiraPontosMemoria;
import br.edu.ucsal.pokesal.infraestrutura.NotificadorResultadoConsole;
import br.edu.ucsal.pokesal.resultado.ParticipanteResultado;
import br.edu.ucsal.pokesal.resultado.ResultadoBatalha;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdaptadoresAutoraisTest {
  @Test
  void acumulaVitoriasDaSessaoSemMisturarTreinadores() {
    CarteiraPontosMemoria carteira = new CarteiraPontosMemoria();
    assertEquals(0, carteira.saldo(1));
    carteira.creditar(1, 150);
    carteira.creditar(1, 100);
    carteira.creditar(2, 100);
    assertEquals(250, carteira.saldo(1));
    assertEquals(100, carteira.saldo(2));
    assertEquals(0, carteira.saldo(3));
  }

  @Test
  void avisoNoConsoleIdentificaDestinatarioBatalhaVencedorETurnos() {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    NotificadorResultadoConsole notificador = new NotificadorResultadoConsole(
        new PrintStream(buffer, true, StandardCharsets.UTF_8));
    ResultadoBatalha resultado = new ResultadoBatalha(9,
        new ParticipanteResultado(1, "Ana", 1, "CharSal"),
        new ParticipanteResultado(2, "Bia", 2, "BulbaSal"), 3);
    notificador.notificar(2, resultado);
    assertEquals("Aviso ao treinador 2: batalha 9 encerrada em 3 turno(s). "
        + "Vencedor: Ana (CharSal). Perdedor: Bia (BulbaSal)."
        + System.lineSeparator(), buffer.toString(StandardCharsets.UTF_8));
  }
}
