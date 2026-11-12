package br.edu.ucsal.pokesal.arena;

import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.enums.TipoTerreno;
import br.edu.ucsal.pokesal.pokesal.Pokesal;
import java.util.Objects;

/** Arena cujo terreno modifica o dano dos golpes e a recuperação de HP. */
public class Arena {

  private final String nome;
  private final TipoTerreno tipoTerreno;
  private final ServicoTerreno servicoTerreno;

  /**
   * Cria uma arena com o terreno informado.
   *
   * @param nome nome da arena
   * @param tipoTerreno terreno que determina os efeitos da arena
   * @param servicoTerreno serviço que consulta as condições atuais
   */
  public Arena(String nome, TipoTerreno tipoTerreno, ServicoTerreno servicoTerreno) {
    this.nome = nome;
    this.tipoTerreno = Objects.requireNonNull(tipoTerreno);
    this.servicoTerreno = Objects.requireNonNull(servicoTerreno);
  }

  /**
   * Calcula o bônus de dano do terreno para o tipo do golpe.
   *
   * @param tipoGolpe tipo elemental do golpe, ou {@code null} para o multiplicador neutro
   * @return multiplicador de dano aplicado pelo terreno
   */
  public double calcularMultiplicadorDano(TipoElemental tipoGolpe) {
    return servicoTerreno.obterEfeito(tipoTerreno).multiplicadorPara(tipoGolpe);
  }

  /**
   * Recupera HP de um Pokésal de planta no canteiro central ao fim do turno.
   *
   * @param alvo Pokésal que recebe o efeito; alvos nulos ou derrotados são ignorados
   */
  public void aplicarEfeitoFimDoTurno(Pokesal alvo) {
    if (alvo == null || alvo.estaDerrotado()) {
      return;
    }

    EfeitoTerreno efeito = servicoTerreno.obterEfeito(tipoTerreno);
    if (alvo.getTipoElemental() == TipoElemental.PLANTA) {
      int cura = (int) Math.round(alvo.getHpMaximo() * efeito.curaPlanta());
      alvo.recuperarHp(cura);
    }
  }

  /**
   * Retorna o nome de exibição da arena.
   *
   * @return nome da arena
   */
  public String getNome() {
    return nome;
  }

  /**
   * Retorna o terreno que determina os efeitos da arena.
   *
   * @return tipo de terreno da arena
   */
  public TipoTerreno getTipoTerreno() {
    return tipoTerreno;
  }
}
