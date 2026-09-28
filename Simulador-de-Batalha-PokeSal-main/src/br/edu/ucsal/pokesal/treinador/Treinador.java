package br.edu.ucsal.pokesal.treinador;

import br.edu.ucsal.pokesal.pokesal.Pokesal;

/** Representa um treinador, seu Pokésal e a mochila de itens. */
public class Treinador {
  private int id;
  private String nome;
  private Pokesal pokesal;
  private Mochila mochila;

  /**
   * Cria um treinador com o Pokésal e a mochila informados.
   *
   * @param id identificador do treinador
   * @param nome nome do treinador
   * @param pokesal Pokésal inicial, ou {@code null} se ainda não foi escolhido
   * @param mochila mochila que armazena os itens do treinador
   */
  public Treinador(int id, String nome, Pokesal pokesal, Mochila mochila) {
    this.id = id;
    this.nome = nome;
    this.pokesal = pokesal;
    this.mochila = mochila;
  }

  /**
   * Retorna o nome usado para identificar o treinador na batalha.
   *
   * @return nome de exibição do treinador
   */
  public String getNome() {
    return nome;
  }

  /**
   * Retorna o Pokésal escolhido para participar da batalha.
   *
   * @return Pokésal do treinador, ou {@code null} se ainda não foi escolhido
   */
  public Pokesal getPokesal() {
    return pokesal;
  }

  /**
   * Retorna a mochila usada para armazenar e consumir os itens do treinador.
   *
   * @return mochila associada ao treinador
   */
  public Mochila getMochila() {
    return mochila;
  }
}
