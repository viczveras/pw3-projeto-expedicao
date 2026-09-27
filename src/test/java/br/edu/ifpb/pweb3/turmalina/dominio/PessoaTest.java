package br.edu.ifpb.pweb3.turmalina.dominio;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.Titulacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Endereco;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PessoaTest {

    @Test
    void normalizaCpfFormatado() {
        assertEquals("11144477735", criarPessoa("111.444.777-35", "ana@ifpb.edu.br").getCpf());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1114447773", "111444777351", "111.444.777"})
    void rejeitaCpfComQuantidadeInvalidaDeDigitos(String cpf) {
        assertThrows(IllegalArgumentException.class, () -> criarPessoa(cpf, "ana@ifpb.edu.br"));
    }

    @Test
    void normalizaEmailNoCadastroENaAlteracao() {
        Pessoa pessoa = criarPessoa("11144477735", "  Ana.Lima@IFPB.edu.br ");

        assertEquals("ana.lima@ifpb.edu.br", pessoa.getEmail());

        pessoa.setEmail(" CONTATO@Turmalina.org ");
        assertEquals("contato@turmalina.org", pessoa.getEmail());
    }

    @Test
    void iniciaAtivaEPodeSerInativadaEReativada() {
        Pessoa pessoa = criarPessoa("11144477735", "ana@ifpb.edu.br");
        assertTrue(pessoa.isAtivo());

        pessoa.inativar();
        assertFalse(pessoa.isAtivo());

        pessoa.reativar();
        assertTrue(pessoa.isAtivo());
    }

    @Test
    void exigeCamposObrigatorios() {
        LocalDate nascimento = LocalDate.of(1985, 3, 14);
        Endereco endereco = endereco();

        assertThrows(NullPointerException.class, () -> new Pessoa(null, "11144477735", nascimento,
                "ana@ifpb.edu.br", "83999990000", endereco));
        assertThrows(NullPointerException.class, () -> new Pessoa("Ana", null, nascimento,
                "ana@ifpb.edu.br", "83999990000", endereco));
        assertThrows(NullPointerException.class, () -> new Pessoa("Ana", "11144477735", null,
                "ana@ifpb.edu.br", "83999990000", endereco));
        assertThrows(NullPointerException.class, () -> new Pessoa("Ana", "11144477735", nascimento,
                null, "83999990000", endereco));
        assertThrows(NullPointerException.class, () -> new Pessoa("Ana", "11144477735", nascimento,
                "ana@ifpb.edu.br", "83999990000", null));
    }

    @Test
    void pesquisadorHerdaCadastroDePessoa() {
        Pesquisador pesquisador = new Pesquisador("Ana Beatriz Lima", "111.444.777-35",
                LocalDate.of(1985, 3, 14), "ANA@ifpb.edu.br", "83999990000", endereco(),
                "IFPB-0042", "Bioespeleologia", Titulacao.DOUTORADO, new BigDecimal("180.00"));

        Pessoa comoPessoa = assertInstanceOf(Pessoa.class, pesquisador);
        assertEquals("11144477735", comoPessoa.getCpf());
        assertEquals("ana@ifpb.edu.br", comoPessoa.getEmail());
        assertTrue(comoPessoa.isAtivo());
        assertEquals(Titulacao.DOUTORADO, pesquisador.getTitulacao());
        assertEquals(new BigDecimal("180.00"), pesquisador.getValorDiarioBolsa());
    }

    @Test
    void pesquisadorExigeAtributosDaEspecializacao() {
        assertThrows(NullPointerException.class, () -> new Pesquisador("Ana", "11144477735",
                LocalDate.of(1985, 3, 14), "ana@ifpb.edu.br", "83999990000", endereco(),
                "IFPB-0042", "Bioespeleologia", null, BigDecimal.TEN));
        assertThrows(NullPointerException.class, () -> new Pesquisador("Ana", "11144477735",
                LocalDate.of(1985, 3, 14), "ana@ifpb.edu.br", "83999990000", endereco(),
                "IFPB-0042", "Bioespeleologia", Titulacao.MESTRADO, null));
    }

    @Test
    void distinguePessoasTransientesComMesmoCpf() {
        Pessoa primeira = criarPessoa("11144477735", "ana@ifpb.edu.br");
        Pessoa segunda = criarPessoa("11144477735", "ana@ifpb.edu.br");

        assertNotEquals(primeira, segunda);
        assertEquals(primeira, primeira);
    }

    static Endereco endereco() {
        return new Endereco("Av. Primeiro de Maio", "720", null, "Jaguaribe",
                "João Pessoa", UnidadeFederativa.PB, "58015-435");
    }

    private Pessoa criarPessoa(String cpf, String email) {
        return new Pessoa("Ana Beatriz Lima", cpf, LocalDate.of(1985, 3, 14), email, "83999990000", endereco());
    }
}
