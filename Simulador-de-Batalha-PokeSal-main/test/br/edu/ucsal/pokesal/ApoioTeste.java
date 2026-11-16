package br.edu.ucsal.pokesal;

import br.edu.ucsal.pokesal.aleatorio.GeradorAleatorio;
import br.edu.ucsal.pokesal.arena.Arena;
import br.edu.ucsal.pokesal.arena.ServicoTerrenoClimatico;
import br.edu.ucsal.pokesal.batalha.Batalha;
import br.edu.ucsal.pokesal.batalha.RegistroBatalha;
import br.edu.ucsal.pokesal.enums.Clima;
import br.edu.ucsal.pokesal.enums.Status;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.enums.TipoTerreno;
import br.edu.ucsal.pokesal.pokesal.Golpe;
import br.edu.ucsal.pokesal.pokesal.Pokesal;
import br.edu.ucsal.pokesal.resultado.RepositorioResultados;
import br.edu.ucsal.pokesal.treinador.Mochila;
import br.edu.ucsal.pokesal.treinador.Treinador;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Scanner;


// Fábrica de objetos de apoio para os testes unitários do pacote.
final class ApoioTeste {

  private static final ZoneId FUSO_HORARIO_BAHIA = ZoneId.of("America/Bahia");
  private static final String INSTANTE_FIXO_PADRAO = "2026-11-02T15:00:00Z";

  private static final int PODER_GOLPE_PADRAO = 20;
  private static final int MAX_USOS_GOLPE_PADRAO = 10;
  private static final double CHANCE_STATUS_NENHUMA = 0;


  private static final int HP_MAXIMO_PADRAO = 100;
  private static final int ATAQUE_PADRAO = 50;
  private static final int DEFESA_PADRAO = 50;


  private static final int ID_BATALHA_PADRAO = 1;
  private static final double VALOR_ALEATORIO_FIXO = 0.5;

  private ApoioTeste() {}

// Cria um relógio congelado no instante informado, no fuso horário da Bahia.
  static Clock relogio(String instante) {
    return Clock.fixed(Instant.parse(instante), FUSO_HORARIO_BAHIA);
  }


   // Cria uma arena de teste com relógio fixo e clima previsível para o terreno informado.
  static Arena arena(TipoTerreno terreno) {
    Clima climaFixo = terreno == TipoTerreno.POCA_DE_CHUVA ? Clima.CHUVOSO : Clima.ENSOLARADO;
    ServicoTerrenoClimatico servicoTerreno =
        new ServicoTerrenoClimatico(relogio(INSTANTE_FIXO_PADRAO), () -> climaFixo);

    return new Arena("Teste", terreno, servicoTerreno);
  }

  //Cria um golpe simples do tipo {@link TipoElemental#FOGO} com a precisão informada.
  static Golpe golpe(double precisao) {
    return new Golpe(
        "Golpe",
        PODER_GOLPE_PADRAO,
        precisao,
        TipoElemental.FOGO,
        MAX_USOS_GOLPE_PADRAO,
        Status.NENHUM,
        CHANCE_STATUS_NENHUMA);
  }

 //Cria um treinador de teste com mochila vazia e um Pokésal do tipo {@link TipoElemental#FOGO}.
  static Treinador treinador(int id, int velocidade, Golpe golpe) {
    Pokesal pokesal =
        new Pokesal(
            id,
            "Pokésal " + id,
            HP_MAXIMO_PADRAO,
            ATAQUE_PADRAO,
            DEFESA_PADRAO,
            velocidade,
            TipoElemental.FOGO,
            List.of(golpe));

    return new Treinador(id, "Treinador " + id, pokesal, new Mochila());
  }

 //Cria uma batalha determinística entre dois treinadores para execução em testes.
  static Batalha batalha(Treinador primeiro, Treinador segundo, Arena arena, Scanner scanner) {
    GeradorAleatorio geradorFixo = () -> VALOR_ALEATORIO_FIXO;
    RepositorioResultados repositorioVazio = resultado -> {};
    RegistroBatalha registroVazio = evento -> {};

    return new Batalha(
        ID_BATALHA_PADRAO,
        primeiro,
        segundo,
        arena,
        scanner,
        geradorFixo,
        repositorioVazio,
        registroVazio);
  }
}
