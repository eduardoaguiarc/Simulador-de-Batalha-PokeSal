package br.edu.ucsal.pokesal.item;

import br.edu.ucsal.pokesal.pokesal.Pokesal;

/** Define os dados e as operações dos itens utilizados nos Pokesal. */
public abstract class Item {
  private int id;
  private String nome;

  /**
   * Cria um item com o identificador e o nome informados.
   *
   * @param id identificador do item
   * @param nome nome de exibição do item
   */
  public Item(int id, String nome) {
    this.id = id;
    this.nome = nome;
  }

  /**
   * Retorna o nome usado para exibir o item na mochila.
   *
   * @return nome de exibição do item
   */
  public String getNome() {
    return nome;
  }

  /**
   * Verifica se o item pode ser usado no Pokesal informado.
   *
   * @param pokesal Pokesal que receberá o efeito
   * @return {@code true} se o uso do item for permitido
   */
  public abstract boolean podeUsar(Pokesal pokesal);

  /**
   * Aplica o efeito do item ao Pokesal informado quando seu uso for permitido.
   *
   * @param pokesal Pokesal que receberá o efeito
   */
  public abstract void aplicarEfeito(Pokesal pokesal);
}
