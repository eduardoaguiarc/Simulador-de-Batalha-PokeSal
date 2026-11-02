package br.edu.ucsal.pokesal.arena;

import br.edu.ucsal.pokesal.enums.TipoTerreno;

/** Consulta o efeito atual de um terreno, sem depender de uma fonte concreta de tempo. */
@FunctionalInterface
public interface ServicoTerreno {
  /** Obtém os modificadores válidos no momento da consulta. */
  EfeitoTerreno obterEfeito(TipoTerreno terreno);
}
