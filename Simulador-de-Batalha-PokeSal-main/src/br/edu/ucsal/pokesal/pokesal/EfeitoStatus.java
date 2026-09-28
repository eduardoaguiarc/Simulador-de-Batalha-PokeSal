package br.edu.ucsal.pokesal.pokesal;

import br.edu.ucsal.pokesal.enums.Status;

/**
 * Representa um status e seus efeitos sobre os atributos e o fim de turno.
 */
public class EfeitoStatus {

  private static final double REDUCAO_ATAQUE_QUEIMADO = 0.5;
  private static final double REDUCAO_VELOCIDADE_PARALISADO = 0.5;
  private static final double DANO_QUEIMADO_PERCENTUAL = 0.065;
  private static final double DANO_VENENO_PERCENTUAL = 0.125;

  private Status tipo;
  private int turnosAtivos;

  /**
   * Cria um efeito com o status e a duração informados.
   *
   * @param tipo status aplicado
   * @param turnosAtivos duração em turnos; zero mantém o efeito sem expiração automática
   */
  public EfeitoStatus(Status tipo, int turnosAtivos) {
    this.tipo = tipo;
    this.turnosAtivos = turnosAtivos;
  }

  /**
   * Retorna o status atual, que passa a nenhum quando sua duração expira.
   *
   * @return status representado por este efeito
   */
  public Status getTipo() {
    return tipo;
  }

  /**
   * Aplica o dano do status e atualiza sua duração ao encerrar o turno.
   *
   * @param alvo Pokésal que recebe os efeitos do status
   */
  public void aplicarFimDoTurno(Pokesal alvo) {
    if (alvo == null || tipo == null || tipo == Status.NENHUM || alvo.estaDerrotado()) {
      return;
    }

    switch (tipo) {
      case QUEIMADO -> {
        int danoQueimadura = (int) Math.round(alvo.getHpMaximo() * DANO_QUEIMADO_PERCENTUAL);
        alvo.receberDano(danoQueimadura);
      }
      case ENVENENADO -> {
        int danoVeneno = (int) Math.round(alvo.getHpMaximo() * DANO_VENENO_PERCENTUAL);
        alvo.receberDano(danoVeneno);
      }
      default -> {
        // Os demais status não causam dano ao final do turno.
      }
    }

    if (turnosAtivos > 0) {
      turnosAtivos--;
      if (turnosAtivos == 0) {
        tipo = Status.NENHUM;
      }
    }
  }

  /**
   * Calcula o ataque considerando a redução causada por queimadura.
   *
   * @param ataqueBase ataque sem modificadores de status
   * @return ataque após a aplicação do status
   */
  public int calcularAtaqueEfetivo(int ataqueBase) {
    if (tipo == Status.QUEIMADO) {
      return (int) Math.round(ataqueBase * REDUCAO_ATAQUE_QUEIMADO);
    }
    return ataqueBase;
  }

  /**
   * Calcula a velocidade considerando a redução causada por paralisia.
   *
   * @param velocidadeBase velocidade sem modificadores de status
   * @return velocidade após a aplicação do status
   */
  public int calcularVelocidadeEfetiva(int velocidadeBase) {
    if (tipo == Status.PARALISADO) {
      return (int) Math.round(velocidadeBase * REDUCAO_VELOCIDADE_PARALISADO);
    }
    return velocidadeBase;
  }
}
