package br.edu.ucsal.pokesal.pokesal;

import br.edu.ucsal.pokesal.enums.Status;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa um Pokésal com seus atributos, golpes e status de batalha.
 */
public class Pokesal {

  private static final int MAX_GOLPES = 4;

  private final int id;
  private final String nome;
  private int hpAtual;
  private final int hpMaximo;
  private final int ataque;
  private final int defesa;
  private final int velocidade;
  private final TipoElemental tipoElemental;
  private final List<Golpe> golpes;
  private EfeitoStatus efeitoStatus;

  /**
   * Cria um Pokésal com HP completo e os golpes informados.
   *
   * @param id identificador do Pokésal
   * @param nome nome do Pokésal
   * @param hpMaximo quantidade máxima de HP, maior que zero
   * @param ataque atributo de ataque, maior ou igual a zero
   * @param defesa atributo de defesa, maior ou igual a zero
   * @param velocidade atributo de velocidade, maior ou igual a zero
   * @param tipoElemental tipo elemental do Pokésal
   * @param golpes lista inicial com até quatro golpes
   * @throws IllegalArgumentException se os atributos ou a lista de golpes forem inválidos
   */
  public Pokesal(
      int id,
      String nome,
      int hpMaximo,
      int ataque,
      int defesa,
      int velocidade,
      TipoElemental tipoElemental,
      List<Golpe> golpes) {
    if (nome == null || nome.isBlank()) {
      throw new IllegalArgumentException("O nome do Pokésal é obrigatório!");
    }

    if (hpMaximo <= 0) {
      throw new IllegalArgumentException("O HP máximo deve ser positivo!");
    }

    if (ataque < 0 || defesa < 0 || velocidade < 0) {
      throw new IllegalArgumentException("Os atributos não podem ser negativos!");
    }

    if (tipoElemental == null) {
      throw new IllegalArgumentException("O tipo elemental é obrigatório!");
    }

    if (golpes == null || golpes.size() > MAX_GOLPES) {
      throw new IllegalArgumentException("Informe uma lista com até quatro golpes!");
    }

    this.id = id;
    this.nome = nome;
    this.hpMaximo = hpMaximo;
    this.hpAtual = hpMaximo;
    this.ataque = ataque;
    this.defesa = defesa;
    this.velocidade = velocidade;
    this.tipoElemental = tipoElemental;
    this.golpes = new ArrayList<>();

    for (Golpe golpe : golpes) {
      adicionarGolpe(golpe);
    }

    this.efeitoStatus = new EfeitoStatus(Status.NENHUM, 0);
  }

  /**
   * Adiciona um golpe à lista, respeitando o limite de quatro golpes.
   *
   * @param golpe golpe a adicionar
   * @throws IllegalArgumentException se o golpe for nulo
   * @throws IllegalStateException se a lista já possuir quatro golpes
   */
  public void adicionarGolpe(Golpe golpe) {
    if (golpe == null) {
      throw new IllegalArgumentException("O golpe não pode ser nulo!");
    }

    if (golpes.size() >= MAX_GOLPES) {
      throw new IllegalStateException("O Pokésal já possui quatro golpes!");
    }

    golpes.add(golpe);
  }

  /**
   * Restaura a quantidade de usos de todos os golpes do Pokésal.
   */
  public void resetarUsosDosGolpes() {
    for (Golpe golpe : golpes) {
      golpe.resetarUsos();
    }
  }

  /**
   * Reduz o HP atual sem permitir valores abaixo de zero.
   *
   * @param dano quantidade de HP a remover
   * @throws IllegalArgumentException se o dano for negativo
   */
  public void receberDano(int dano) {
    if (dano < 0) {
      throw new IllegalArgumentException("O dano não pode ser negativo!");
    }

    hpAtual = Math.max(0, hpAtual - dano);
  }

  /**
   * Recupera HP sem ultrapassar o HP máximo.
   *
   * @param quantidade quantidade de HP a recuperar
   * @throws IllegalArgumentException se a quantidade for negativa
   */
  public void recuperarHp(int quantidade) {
    if (quantidade < 0) {
      throw new IllegalArgumentException("A quantidade de cura não pode ser negativa!");
    }

    int hpFaltando = hpMaximo - hpAtual;
    int curaAplicada = Math.min(quantidade, hpFaltando);

    hpAtual += curaAplicada;
  }

  /**
   * Verifica se o Pokésal está sem HP.
   *
   * @return verdadeiro quando o HP atual é zero
   */
  public boolean estaDerrotado() {
    return hpAtual == 0;
  }

  /**
   * Substitui o status atual ou o remove quando o novo status é nenhum.
   *
   * @param status status a aplicar
   * @throws IllegalArgumentException se o status for nulo
   */
  public void aplicarStatus(Status status) {
    if (status == null) {
      throw new IllegalArgumentException("O status não pode ser nulo!");
    }

    if (status == Status.NENHUM) {
      removerStatus();
      return;
    }

    efeitoStatus = new EfeitoStatus(status, 0);
  }

  /**
   * Remove o status atual do Pokésal.
   */
  public void removerStatus() {
    efeitoStatus = new EfeitoStatus(Status.NENHUM, 0);
  }

  /**
   * Retorna o status atual do Pokésal.
   *
   * @return status aplicado
   */
  public Status obterStatus() {
    return efeitoStatus.getTipo();
  }

  /**
   * Retorna o ataque considerando o status atual.
   *
   * @return ataque após os modificadores de status
   */
  public int obterAtaqueEfetivo() {
    return efeitoStatus.calcularAtaqueEfetivo(ataque);
  }

  /**
   * Retorna a velocidade considerando o status atual.
   *
   * @return velocidade após os modificadores de status
   */
  public int obterVelocidadeEfetiva() {
    return efeitoStatus.calcularVelocidadeEfetiva(velocidade);
  }

  /**
   * Retorna o nome usado para identificar o Pokésal na batalha.
   *
   * @return nome de exibição do Pokésal
   */
  public String getNome() {
    return nome;
  }

  /**
   * Retorna os pontos de vida restantes após danos e curas.
   *
   * @return HP atual do Pokésal
   */
  public int getHpAtual() {
    return hpAtual;
  }

  /**
   * Retorna o limite de pontos de vida usado na recuperação de HP.
   *
   * @return HP máximo do Pokésal
   */
  public int getHpMaximo() {
    return hpMaximo;
  }

  /**
   * Retorna o atributo de defesa usado para reduzir o dano recebido.
   *
   * @return defesa do Pokésal
   */
  public int getDefesa() {
    return defesa;
  }

  /**
   * Retorna o tipo usado para determinar a efetividade dos golpes recebidos.
   *
   * @return tipo elemental do Pokésal
   */
  public TipoElemental getTipoElemental() {
    return tipoElemental;
  }

  /**
   * Retorna uma visão da lista de golpes que impede adicionar ou remover elementos.
   *
   * @return lista não modificável que acompanha os golpes adicionados ao Pokésal
   */
  public List<Golpe> getGolpes() {
    return Collections.unmodifiableList(golpes);
  }

  /**
   * Retorna o efeito responsável pelo status atual e por sua duração.
   *
   * @return efeito de status associado ao Pokésal
   */
  public EfeitoStatus getEfeitoStatus() {
    return efeitoStatus;
  }
}
