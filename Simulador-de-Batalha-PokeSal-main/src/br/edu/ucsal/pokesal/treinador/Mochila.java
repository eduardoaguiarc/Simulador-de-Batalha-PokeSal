package br.edu.ucsal.pokesal.treinador;

import br.edu.ucsal.pokesal.item.Item;
import java.util.ArrayList;
import java.util.List;

/** Armazena os itens disponíveis para uso por um treinador. */
public class Mochila {

  private List<Item> itens;

  /** Cria uma mochila sem itens. */
  public Mochila() {
    this.itens = new ArrayList<>();
  }

  /**
   * Adiciona um item à mochila.
   *
   * @param item item a armazenar
   * @throws IllegalArgumentException se o item for nulo
   */
  public void adicionarItem(Item item) {
    if (item == null) {
      throw new IllegalArgumentException("O item não pode ser nulo.");
    }

    itens.add(item);
  }

  /**
   * Retorna uma cópia não modificável da lista de itens presentes na mochila.
   *
   * @return lista dos itens existentes no momento da consulta
   */
  public List<Item> getItens() {
    return List.copyOf(itens);
  }

  /**
   * Remove a primeira ocorrência de um item da mochila.
   *
   * @param item item a remover
   * @return {@code true} se o item foi removido; {@code false} se ele for nulo ou ausente
   */
  public boolean removerItem(Item item) {
    if (item == null) {
      return false;
    }

    return itens.remove(item);
  }
}
