package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.NivelCertificacao;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuiaEspeleologiaTest {

    private static final LocalDate VALIDADE = LocalDate.of(2027, 6, 30);

    @Test
    void iniciaSemExpedicoesEContaCadaConclusao() {
        GuiaEspeleologia guia = criarGuia();
        assertEquals(0, guia.getExpedicoesConcluidas());

        guia.registrarExpedicaoConcluida();
        guia.registrarExpedicaoConcluida();

        assertEquals(2, guia.getExpedicoesConcluidas());
    }

    @Test
    void certificacaoValeAteODiaDaValidadeInclusive() {
        GuiaEspeleologia guia = criarGuia();

        assertTrue(guia.certificacaoValidaEm(VALIDADE.minusDays(1)));
        assertTrue(guia.certificacaoValidaEm(VALIDADE));
        assertFalse(guia.certificacaoValidaEm(VALIDADE.plusDays(1)));
    }

    @Test
    void renovacaoAtualizaNivelEValidade() {
        GuiaEspeleologia guia = criarGuia();
        LocalDate novaValidade = VALIDADE.plusYears(2);

        guia.renovarCertificacao(NivelCertificacao.AVANCADO, novaValidade);

        assertEquals(NivelCertificacao.AVANCADO, guia.getNivelCertificacao());
        assertEquals(novaValidade, guia.getValidadeCertificacao());
        assertTrue(guia.certificacaoValidaEm(VALIDADE.plusDays(1)));
    }

    @Test
    void exigeAtributosDaEspecializacao() {
        assertThrows(NullPointerException.class, () -> new GuiaEspeleologia("Maria Eduarda Souto",
                "390.533.447-05", LocalDate.of(1988, 6, 21), "duda@turmalina.org", "83988880000",
                PessoaTest.endereco(), null, NivelCertificacao.BASICO, VALIDADE));
        assertThrows(NullPointerException.class, () -> new GuiaEspeleologia("Maria Eduarda Souto",
                "390.533.447-05", LocalDate.of(1988, 6, 21), "duda@turmalina.org", "83988880000",
                PessoaTest.endereco(), "SBE-1234", NivelCertificacao.BASICO, null));
    }

    private GuiaEspeleologia criarGuia() {
        return new GuiaEspeleologia("Maria Eduarda Souto", "390.533.447-05", LocalDate.of(1988, 6, 21),
                "duda@turmalina.org", "83988880000", PessoaTest.endereco(), "SBE-1234",
                NivelCertificacao.INTERMEDIARIO, VALIDADE);
    }
}
