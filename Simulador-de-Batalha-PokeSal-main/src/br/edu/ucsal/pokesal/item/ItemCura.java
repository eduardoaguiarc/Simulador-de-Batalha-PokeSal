package br.edu.ucsal.pokesal.item;

import br.edu.ucsal.pokesal.pokesal.Pokesal;

/** Item que recupera pontos de vida de um Pokesal que ainda pode batalhar. */
public class ItemCura extends Item {
  private int quantidadeCura;

  /**
   * Cria um item de cura com a quantidade de pontos de vida a recuperar.
   *
   * @param id identificador do item
   * @param nome nome de exibição do item
   * @param quantidadeCura quantidade de pontos de vida recuperados pelo item
   */
  public ItemCura(int id, String nome, int quantidadeCura) {
    super(id, nome);
    this.quantidadeCura = quantidadeCura;
  }

  /**
   * Verifica se o Pokésal ainda pode batalhar, mesmo que esteja com HP completo.
   *
   * @param pokesal Pokésal que receberá a cura
   * @return {@code true} se o Pokésal não estiver derrotado
   * @throws NullPointerException se o Pokésal for nulo
   */
  @Override
  public boolean podeUsar(Pokesal pokesal) {
    return !pokesal.estaDerrotado();
  }

  /**
   * Recupera HP de um Pokésal apto a batalhar, respeitando seu HP máximo.
   *
   * @param pokesal Pokésal que receberá a cura; alvos derrotados geram uma mensagem de falha
   * @throws NullPointerException se o Pokésal for nulo
   * @throws IllegalArgumentException se a cura aplicada tiver quantidade negativa
   */
  @Override
  public void aplicarEfeito(Pokesal pokesal) {
    if (podeUsar(pokesal)) {
      pokesal.recuperarHp(quantidadeCura);
    } else {
      System.out.println("Não foi possível aplicar efeito.");
    }
  }
}
