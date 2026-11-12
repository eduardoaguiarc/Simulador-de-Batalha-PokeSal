package br.edu.ucsal.pokesal.batalha;

/** Evento imutável; valor indica dano ou recuperação de HP, quando aplicável. */
public record EventoBatalha(
    int idBatalha, int turno, TipoEvento tipo, String descricao, int valor, boolean critico) {}
