package br.edu.ucsal.pokesal.resultado;

/** Porta de persistência dos resultados de cada batalha. */
@FunctionalInterface
public interface RepositorioResultados {
  /**
   * Salva um resultado; falhas são tratadas pela batalha sem desfazer o
   * resultado.
   */
  void salvar(ResultadoBatalha resultado) throws Exception;
}
