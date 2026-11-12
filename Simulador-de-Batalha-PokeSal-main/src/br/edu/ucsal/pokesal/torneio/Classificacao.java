package br.edu.ucsal.pokesal.torneio;

import br.edu.ucsal.pokesal.treinador.Treinador;

/** Desempenho de um treinador no torneio atual. */
public record Classificacao(Treinador treinador, int vitorias, int derrotas) {}
