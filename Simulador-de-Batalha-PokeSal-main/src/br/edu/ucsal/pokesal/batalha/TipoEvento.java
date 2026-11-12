package br.edu.ucsal.pokesal.batalha;

/** A ordem das constantes define a ordem de envio dos eventos de cada turno. */
public enum TipoEvento {
  ATAQUE,
  DANO,
  EFEITO_STATUS,
  EFEITO_TERRENO,
  USO_ITEM,
  ERRO_REPOSITORIO,
  ERRO_PREMIACAO,
  ERRO_NOTIFICACAO
}
