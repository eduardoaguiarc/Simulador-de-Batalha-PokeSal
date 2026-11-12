package br.edu.ucsal.pokesal.resultado;

import br.edu.ucsal.pokesal.batalha.EventoBatalha;
import br.edu.ucsal.pokesal.batalha.RegistroBatalha;
import br.edu.ucsal.pokesal.batalha.TipoEvento;
import java.util.Objects;

/** Premia o vencedor e avisa os participantes, isolando falhas dos colaboradores. */
public final class ServicoPosBatalha implements PosBatalha {
  private static final int PONTOS_VITORIA = 100;
  private static final int BONUS_VITORIA_RAPIDA = 50;
  private static final int LIMITE_TURNOS_RAPIDA = 3;
  private final CarteiraPontos carteira;
  private final NotificadorResultado notificador;
  private final RegistroBatalha registro;

  /** Recebe carteira, canal de aviso e registro de erros substituíveis em testes. */
  public ServicoPosBatalha(
      CarteiraPontos carteira, NotificadorResultado notificador, RegistroBatalha registro) {
    this.carteira = Objects.requireNonNull(carteira);
    this.notificador = Objects.requireNonNull(notificador);
    this.registro = Objects.requireNonNull(registro);
  }

  @Override
  public void processar(ResultadoBatalha resultado) {
    Objects.requireNonNull(resultado);
    // Sem turno disputado não há premiação; ainda se comunica o resultado.
    if (resultado.numeroTurnos() > 0) {
      int pontos =
          PONTOS_VITORIA
              + (resultado.numeroTurnos() <= LIMITE_TURNOS_RAPIDA ? BONUS_VITORIA_RAPIDA : 0);
      try {
        carteira.creditar(resultado.vencedor().idTreinador(), pontos);
      } catch (RuntimeException erro) {
        registrarFalha(resultado, TipoEvento.ERRO_PREMIACAO, erro);
      }
    }
    notificar(resultado.vencedor().idTreinador(), resultado);
    notificar(resultado.perdedor().idTreinador(), resultado);
  }

  private void notificar(int idTreinador, ResultadoBatalha resultado) {
    try {
      notificador.notificar(idTreinador, resultado);
    } catch (RuntimeException erro) {
      registrarFalha(resultado, TipoEvento.ERRO_NOTIFICACAO, erro);
    }
  }

  private void registrarFalha(ResultadoBatalha resultado, TipoEvento tipo, RuntimeException erro) {
    registro.registrar(
        new EventoBatalha(
            resultado.idBatalha(),
            resultado.numeroTurnos(),
            tipo,
            erro.getClass().getSimpleName() + ": " + erro.getMessage(),
            0,
            false));
  }
}
