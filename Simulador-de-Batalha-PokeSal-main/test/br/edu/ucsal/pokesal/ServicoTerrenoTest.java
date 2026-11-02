package br.edu.ucsal.pokesal;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.ucsal.pokesal.arena.Arena;
import br.edu.ucsal.pokesal.arena.ServicoTerrenoClimatico;
import br.edu.ucsal.pokesal.enums.Clima;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.enums.TipoTerreno;
import br.edu.ucsal.pokesal.pokesal.Pokesal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ServicoTerrenoTest {
  @ParameterizedTest
  @CsvSource({
      "2026-11-02T08:59:59Z,1.0", "2026-11-02T09:00:00Z,1.15",
      "2026-11-02T20:59:59Z,1.15", "2026-11-02T21:00:00Z,1.0"
  })
  void asfaltoRespeitaLimitesDoDiaNoFusoDoRelogio(String instante, double esperado) {
    var servico = new ServicoTerrenoClimatico(ApoioTeste.relogio(instante), () -> Clima.ENSOLARADO);
    assertEquals(esperado, servico.obterEfeito(TipoTerreno.ASFALTO_QUENTE).multiplicadorFogo());
  }

  @Test
  void climaEConsultadoNovamenteACadaEfeito() {
    var clima = new AtomicReference<>(Clima.ENSOLARADO);
    var servico = new ServicoTerrenoClimatico(
        ApoioTeste.relogio("2026-11-02T15:00:00Z"), clima::get);
    Arena arena = new Arena("Asfalto", TipoTerreno.ASFALTO_QUENTE, servico);
    assertEquals(1.15, arena.calcularMultiplicadorDano(TipoElemental.FOGO));
    clima.set(Clima.NUBLADO);
    assertEquals(1.0, arena.calcularMultiplicadorDano(TipoElemental.FOGO));
    assertEquals(1.0, servico.obterEfeito(TipoTerreno.POCA_DE_CHUVA).multiplicadorAgua());
    clima.set(Clima.CHUVOSO);
    assertEquals(1.0, arena.calcularMultiplicadorDano(TipoElemental.FOGO));
    assertEquals(1.1, servico.obterEfeito(TipoTerreno.POCA_DE_CHUVA).multiplicadorAgua());
  }

  @Test
  void canteiroRespeitaHpMaximoENaoReviveDerrotado() {
    Arena arena = ApoioTeste.arena(TipoTerreno.CANTEIRO_CENTRAL);
    Pokesal planta = new Pokesal(1, "Planta", 100, 50, 50, 50, TipoElemental.PLANTA, List.of());
    planta.receberDano(2);
    arena.aplicarEfeitoFimDoTurno(planta);
    assertEquals(100, planta.getHpAtual());
    planta.receberDano(100);
    arena.aplicarEfeitoFimDoTurno(planta);
    assertEquals(0, planta.getHpAtual());
  }
}
