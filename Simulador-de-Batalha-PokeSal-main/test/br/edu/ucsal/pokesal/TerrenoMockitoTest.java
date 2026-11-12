package br.edu.ucsal.pokesal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.ucsal.pokesal.arena.ServicoTerrenoClimatico;
import br.edu.ucsal.pokesal.enums.Clima;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.enums.TipoTerreno;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TerrenoMockitoTest {
  @Mock private Clock relogio;
  @Mock private Supplier<Clima> clima;
  @InjectMocks private ServicoTerrenoClimatico servico;

  @Test
  void testAsfaltoQuenteApenasDuranteODia() {
    when(clima.get()).thenReturn(Clima.ENSOLARADO);
    when(relogio.getZone()).thenReturn(ZoneId.of("America/Bahia"));
    when(relogio.instant())
        .thenReturn(
            Instant.parse("2026-11-02T08:59:59Z"), // 05:59:59
            Instant.parse("2026-11-02T09:00:00Z"), // 06:00:00
            Instant.parse("2026-11-02T20:59:59Z"), // 17:59:59
            Instant.parse("2026-11-02T21:00:00Z")); // 18:00:00
    for (double esperado : new double[] {1.0, 1.15, 1.15, 1.0}) {
      assertEquals(
          esperado,
          servico.obterEfeito(TipoTerreno.ASFALTO_QUENTE).multiplicadorPara(TipoElemental.FOGO),
          0.0001);
    }
    verify(relogio, times(4)).instant();
    verify(clima, atLeastOnce()).get();
  }
}
