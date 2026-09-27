package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.DatumGeodesico;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.NivelDificuldade;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Localizacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CavernaTest {

    @Test
    void vinculaSetorNosDoisLadosDaAssociacao() {
        Caverna caverna = criarCaverna("Gruta");
        Setor setor = criarSetor("Entrada");

        assertSame(setor, caverna.adicionarSetor(setor));
        assertSame(caverna, setor.getCaverna());
        assertEquals(List.of(setor), caverna.getSetores());
    }

    @Test
    void rejeitaTransferenciaPreservandoAssociacaoOriginal() {
        Caverna original = criarCaverna("Gruta");
        Caverna outra = criarCaverna("Furna");
        Setor setor = original.adicionarSetor(criarSetor("Entrada"));

        assertThrows(IllegalStateException.class, () -> outra.adicionarSetor(setor));

        assertSame(original, setor.getCaverna());
        assertEquals(List.of(setor), original.getSetores());
        assertTrue(outra.getSetores().isEmpty());
    }

    @Test
    void impedeAlteracaoDaColecaoForaDoAgregado() {
        Caverna caverna = criarCaverna("Gruta");

        assertThrows(UnsupportedOperationException.class,
                () -> caverna.getSetores().add(criarSetor("Entrada")));
        assertTrue(caverna.getSetores().isEmpty());
    }

    @Test
    void registraInspecaoEAtualizaPermissaoDeAcesso() {
        Caverna caverna = criarCaverna("Gruta");
        LocalDate data = LocalDate.of(2026, 9, 27);

        caverna.registrarInspecao(data, false);

        assertEquals(data, caverna.getDataUltimaInspecao());
        assertFalse(caverna.isAcessoPermitido());
    }

    @Test
    void distingueSetoresTransientesComMesmosAtributos() {
        Setor primeiro = criarSetor("Entrada");
        Setor segundo = criarSetor("Entrada");

        assertNotEquals(primeiro, segundo);
        assertEquals(primeiro, primeiro);
    }

    @Test
    void rejeitaAdicionarMesmoSetorDuasVezes() {
        Caverna caverna = criarCaverna("Gruta");
        Setor setor = caverna.adicionarSetor(criarSetor("Entrada"));

        assertThrows(IllegalStateException.class, () -> caverna.adicionarSetor(setor));

        assertEquals(List.of(setor), caverna.getSetores());
        assertSame(caverna, setor.getCaverna());
    }

    private Caverna criarCaverna(String nome) {
        return new Caverna(nome, "CANIE-" + nome, "Cabaceiras", UnidadeFederativa.PB,
                new Localizacao(new BigDecimal("-7.48"), new BigDecimal("-36.28"), DatumGeodesico.SIRGAS_2000));
    }

    private Setor criarSetor(String nome) {
        return new Setor(nome, NivelDificuldade.BAIXO, new BigDecimal("10.50"), new BigDecimal("80.00"), false);
    }
}
