package br.edu.ucsal.pokesal.arena;

import br.edu.ucsal.pokesal.enums.Clima;
import br.edu.ucsal.pokesal.enums.TipoTerreno;
import java.time.Clock;
import java.time.LocalTime;
import java.util.Objects;
import java.util.function.Supplier;

/** Calcula os efeitos usando o relógio e a fonte de clima recebidos pelo construtor. */
public final class ServicoTerrenoClimatico implements ServicoTerreno {
  private static final int INICIO_DIA = 6;
  private static final int FIM_DIA = 18;
  private static final double MULTIPLICADOR_FOGO = 1.15;
  private static final double MULTIPLICADOR_AGUA = 1.10;
  private static final double TAXA_CURA_PLANTA = 0.05;
  private static final EfeitoTerreno NEUTRO = new EfeitoTerreno(1.0, 1.0, 0.0);
  private final Clock relogio;
  private final Supplier<Clima> fonteClima;

  /** Recebe o relógio (incluindo seu fuso) e a fonte consultada a cada efeito. */
  public ServicoTerrenoClimatico(Clock relogio, Supplier<Clima> fonteClima) {
    this.relogio = Objects.requireNonNull(relogio);
    this.fonteClima = Objects.requireNonNull(fonteClima);
  }

  @Override
  public EfeitoTerreno obterEfeito(TipoTerreno terreno) {
    Clima clima = Objects.requireNonNull(fonteClima.get());
    int hora = LocalTime.now(relogio).getHour();
    boolean dia = hora >= INICIO_DIA && hora < FIM_DIA;
    return switch (Objects.requireNonNull(terreno)) {
      case ASFALTO_QUENTE ->
          dia && clima == Clima.ENSOLARADO
              ? new EfeitoTerreno(MULTIPLICADOR_FOGO, 1.0, 0.0)
              : NEUTRO;
      case POCA_DE_CHUVA ->
          clima == Clima.CHUVOSO ? new EfeitoTerreno(1.0, MULTIPLICADOR_AGUA, 0.0) : NEUTRO;
      case CANTEIRO_CENTRAL -> new EfeitoTerreno(1.0, 1.0, TAXA_CURA_PLANTA);
    };
  }
}
