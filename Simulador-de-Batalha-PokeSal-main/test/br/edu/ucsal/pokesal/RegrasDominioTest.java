package br.edu.ucsal.pokesal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.ucsal.pokesal.enums.Status;
import br.edu.ucsal.pokesal.enums.TipoElemental;
import br.edu.ucsal.pokesal.item.Antidoto;
import br.edu.ucsal.pokesal.item.ItemCura;
import br.edu.ucsal.pokesal.pokesal.EfeitoStatus;
import br.edu.ucsal.pokesal.pokesal.Golpe;
import br.edu.ucsal.pokesal.pokesal.Pokesal;
import br.edu.ucsal.pokesal.treinador.Mochila;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegrasDominioTest {
  @Mock
  private Pokesal alvo;

  static Stream<Arguments> atributosInvalidos() {
    Golpe golpe = ApoioTeste.golpe(100);
    return Stream.of(
        Arguments.of(null, 100, 50, 50, 10, TipoElemental.FOGO, List.of()),
        Arguments.of(" ", 100, 50, 50, 10, TipoElemental.FOGO, List.of()),
        Arguments.of("Teste", 0, 50, 50, 10, TipoElemental.FOGO, List.of()),
        Arguments.of("Teste", 100, -1, 50, 10, TipoElemental.FOGO, List.of()),
        Arguments.of("Teste", 100, 50, -1, 10, TipoElemental.FOGO, List.of()),
        Arguments.of("Teste", 100, 50, 50, -1, TipoElemental.FOGO, List.of()),
        Arguments.of("Teste", 100, 50, 50, 10, null, List.of()),
        Arguments.of("Teste", 100, 50, 50, 10, TipoElemental.FOGO, null),
        Arguments.of("Teste", 100, 50, 50, 10, TipoElemental.FOGO,
            List.of(golpe, golpe, golpe, golpe, golpe)));
  }

  @ParameterizedTest
  @MethodSource("atributosInvalidos")
  void rejeitaPokesalComAtributosInvalidos(String nome, int hp, int atk, int def, int spd,
      TipoElemental tipo, List<Golpe> golpes) {
    assertThrows(IllegalArgumentException.class,
        () -> new Pokesal(1, nome, hp, atk, def, spd, tipo, golpes));
  }

  @Test
  void impedeOperacoesInvalidasSemAlterarHpGolpesOuStatus() {
    Pokesal pokesal = ApoioTeste.treinador(1, 10, ApoioTeste.golpe(100)).getPokesal();
    assertThrows(IllegalArgumentException.class, () -> pokesal.receberDano(-1));
    assertThrows(IllegalArgumentException.class, () -> pokesal.recuperarHp(-1));
    assertThrows(IllegalArgumentException.class, () -> pokesal.adicionarGolpe(null));
    assertThrows(IllegalArgumentException.class, () -> pokesal.aplicarStatus(null));
    assertEquals(100, pokesal.getHpAtual());
    assertEquals(1, pokesal.getGolpes().size());
    assertEquals(Status.NENHUM, pokesal.obterStatus());
    pokesal.aplicarStatus(Status.QUEIMADO);
    pokesal.aplicarStatus(Status.NENHUM);
    assertEquals(Status.NENHUM, pokesal.obterStatus());
    assertEquals(50, pokesal.obterAtaqueEfetivo());
  }

  @Test
  void queimaduraCausaDanoEReduzAtaqueAteExpirar() {
    when(alvo.getHpMaximo()).thenReturn(100);
    EfeitoStatus efeito = new EfeitoStatus(Status.QUEIMADO, 2);
    assertEquals(25, efeito.calcularAtaqueEfetivo(50));
    assertEquals(50, efeito.calcularVelocidadeEfetiva(50));
    efeito.aplicarFimDoTurno(alvo);
    assertEquals(Status.QUEIMADO, efeito.getTipo());
    efeito.aplicarFimDoTurno(alvo);
    assertEquals(Status.NENHUM, efeito.getTipo());
    efeito.aplicarFimDoTurno(alvo);
    verify(alvo, times(2)).receberDano(7);
    assertEquals(50, efeito.calcularAtaqueEfetivo(50));
  }

  @Test
  void paralisiaReduzVelocidadeSemCausarDano() {
    EfeitoStatus efeito = new EfeitoStatus(Status.PARALISADO, 1);
    assertEquals(25, efeito.calcularVelocidadeEfetiva(50));
    efeito.aplicarFimDoTurno(alvo);
    verify(alvo, never()).receberDano(anyInt());
    assertEquals(Status.NENHUM, efeito.getTipo());
    assertEquals(50, efeito.calcularVelocidadeEfetiva(50));
  }

  @Test
  void efeitoIgnoraAlvosAusentesDerrotadosEStatusAusente() {
    EfeitoStatus veneno = new EfeitoStatus(Status.ENVENENADO, 0);
    assertDoesNotThrow(() -> veneno.aplicarFimDoTurno(null));
    new EfeitoStatus(null, 0).aplicarFimDoTurno(alvo);
    verifyNoInteractions(alvo);
    when(alvo.estaDerrotado()).thenReturn(true);
    veneno.aplicarFimDoTurno(alvo);
    verify(alvo, never()).receberDano(anyInt());
  }

  @Test
  void itensNaoRevivemPokesalDerrotado() {
    when(alvo.estaDerrotado()).thenReturn(true);
    Antidoto antidoto = new Antidoto(1, "Antidoto");
    ItemCura cura = new ItemCura(2, "Potion", 20);
    assertFalse(antidoto.podeUsar(alvo));
    assertFalse(cura.podeUsar(alvo));
    antidoto.aplicarEfeito(alvo);
    cura.aplicarEfeito(alvo);
    verify(alvo, never()).recuperarHp(anyInt());
    verify(alvo, never()).removerStatus();
  }

  @Test
  void mochilaRejeitaNuloEPreservaItensAoRemoverAusente() {
    Mochila mochila = new Mochila();
    ItemCura item = new ItemCura(1, "Potion", 20);
    assertThrows(IllegalArgumentException.class, () -> mochila.adicionarItem(null));
    mochila.adicionarItem(item);
    assertFalse(mochila.removerItem(null));
    assertFalse(mochila.removerItem(new ItemCura(2, "Outra", 20)));
    assertEquals(List.of(item), mochila.getItens());
    assertThrows(UnsupportedOperationException.class, () -> mochila.getItens().clear());
    assertTrue(mochila.removerItem(item));
    assertTrue(mochila.getItens().isEmpty());
  }
}
