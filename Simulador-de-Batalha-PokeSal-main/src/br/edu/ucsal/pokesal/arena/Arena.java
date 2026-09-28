package br.edu.ucsal.pokesal.arena;

import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.enums.TipoTerreno;
import br.edu.ucsal.pokesal.pokesal.Pokesal;

/** Arena cujo terreno modifica o dano dos golpes e a recuperação de HP. */
public class Arena {

  private static final double BONUS_FOGO = 0.15;
  private static final double BONUS_AGUA = 0.10;
  private static final double TAXA_CURA_PLANTA = 0.05;
  private static final double MULTIPLICADOR_NEUTRO = 1.0;

  private String nome;
  private TipoTerreno tipoTerreno;

  /**
   * Cria uma arena com o terreno informado.
   *
   * @param nome nome da arena
   * @param tipoTerreno terreno que determina os efeitos da arena
   */
  public Arena(String nome, TipoTerreno tipoTerreno) {
    this.nome = nome;
    this.tipoTerreno = tipoTerreno;
  }

  /**
   * Calcula o bônus de dano do terreno para o tipo do golpe.
   *
   * @param tipoGolpe tipo elemental do golpe, ou {@code null} para o multiplicador neutro
   * @return multiplicador de dano aplicado pelo terreno
   */
  public double calcularMultiplicadorDano(TipoElemental tipoGolpe) {
    if (tipoGolpe == null) {
      return MULTIPLICADOR_NEUTRO;
    }

    switch (tipoTerreno) {
      case ASFALTO_QUENTE -> {
        if (tipoGolpe == TipoElemental.FOGO) {
          return MULTIPLICADOR_NEUTRO + BONUS_FOGO;
        }
      }

      case POCA_DE_CHUVA -> {
        if (tipoGolpe == TipoElemental.AGUA) {
          return MULTIPLICADOR_NEUTRO + BONUS_AGUA;
        }
      }
      default -> {
        // Os demais terrenos não modificam o dano dos golpes.
      }
    }

    return MULTIPLICADOR_NEUTRO;
  }

  /**
   * Recupera HP de um Pokésal de planta no canteiro central ao fim do turno.
   *
   * @param alvo Pokésal que recebe o efeito; alvos nulos ou derrotados são ignorados
   */
  public void aplicarEfeitoFimDoTurno(Pokesal alvo) {
    if (alvo == null || alvo.estaDerrotado()) {
      return;
    }

    if (tipoTerreno == TipoTerreno.CANTEIRO_CENTRAL
        && alvo.getTipoElemental() == TipoElemental.PLANTA) {
      int cura = (int) Math.round(alvo.getHpMaximo() * TAXA_CURA_PLANTA);
      alvo.recuperarHp(cura);
    }
  }

  /**
   * Retorna o nome de exibição da arena.
   *
   * @return nome da arena
   */
  public String getNome() {
    return nome;
  }

  /**
   * Retorna o terreno que determina os efeitos da arena.
   *
   * @return tipo de terreno da arena
   */
  public TipoTerreno getTipoTerreno() {
    return tipoTerreno;
  }
}
