package br.edu.ucsal.pokesal.enums;

/** Tipos elementais e suas relações de efetividade nos ataques. */
public enum TipoElemental {
  FOGO,
  AGUA,
  PLANTA;

  private static final double MULTIPLICADOR_SUPER_EFETIVO = 2.0;
  private static final double MULTIPLICADOR_POUCO_EFETIVO = 0.5;
  private static final double MULTIPLICADOR_NEUTRO = 1.0;

  /**
   * Calcula a efetividade deste tipo elemental contra o tipo do defensor.
   *
   * @param defensor tipo elemental que recebe o ataque
   * @return multiplicador de dano de acordo com a relação entre os tipos
   * @throws IllegalArgumentException se o tipo do defensor for nulo
   */
  public double calcularMultiplicadorContra(TipoElemental defensor) {
    if (defensor == null) {
      throw new IllegalArgumentException("O tipo do defensor é obrigatório!");
    }

    return switch (this) {
      case FOGO -> switch (defensor) {
        case PLANTA -> MULTIPLICADOR_SUPER_EFETIVO;

        case AGUA -> MULTIPLICADOR_POUCO_EFETIVO;

        case FOGO -> MULTIPLICADOR_NEUTRO;
      };

      case AGUA -> switch (defensor) {
        case FOGO -> MULTIPLICADOR_SUPER_EFETIVO;

        case PLANTA -> MULTIPLICADOR_POUCO_EFETIVO;

        case AGUA -> MULTIPLICADOR_NEUTRO;
      };

      case PLANTA -> switch (defensor) {
        case AGUA -> MULTIPLICADOR_SUPER_EFETIVO;

        case FOGO -> MULTIPLICADOR_POUCO_EFETIVO;

        case PLANTA -> MULTIPLICADOR_NEUTRO;
      };
    };
  }
}
