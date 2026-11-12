package br.edu.ucsal.pokesal.arena;

import br.edu.ucsal.pokesal.enums.TipoElemental;

/** Valores imutáveis do efeito vigente; cura é uma fração do HP máximo. */
public record EfeitoTerreno(double multiplicadorFogo, double multiplicadorAgua, double curaPlanta) {

  /** Obtém o multiplicador correspondente ao tipo do golpe. */
  public double multiplicadorPara(TipoElemental tipo) {
    if (tipo == TipoElemental.FOGO) {
      return multiplicadorFogo;
    }
    if (tipo == TipoElemental.AGUA) {
      return multiplicadorAgua;
    }
    return 1.0;
  }
}
