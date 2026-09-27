package br.edu.ifpb.pweb3.turmalina.app;

import br.edu.ifpb.pweb3.turmalina.dominio.GuiaEspeleologia;
import br.edu.ifpb.pweb3.turmalina.dominio.Pesquisador;
import br.edu.ifpb.pweb3.turmalina.dominio.Pessoa;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.NivelCertificacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.Titulacao;
import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Endereco;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PessoaIT extends IntegracaoPostgres {

    @Test
    void gravaCadaTipoNaPropriaTabelaComDiscriminadorEEnderecoIncorporado() {
        Long pessoa = persistir(criarPessoa(cpf(), email()));
        Long pesquisador = persistir(criarPesquisador(cpf(), email(), registro(), BigDecimal.TEN));
        Long guia = persistir(criarGuia(cpf(), email()));

        transacao(em -> {
            assertEquals("PESSOA", valorNativo(em, "select tipo_pessoa from {h-schema}pessoa where id = ?", pessoa));
            assertEquals("PESQUISADOR", valorNativo(em, "select tipo_pessoa from {h-schema}pessoa where id = ?", pesquisador));
            assertEquals("GUIA", valorNativo(em, "select tipo_pessoa from {h-schema}pessoa where id = ?", guia));
            assertEquals(1L, ((Number) valorNativo(em,
                    "select count(*) from {h-schema}pesquisador where pessoa_id = ?", pesquisador)).longValue());
            assertEquals(1L, ((Number) valorNativo(em,
                    "select count(*) from {h-schema}guia_espeleologia where pessoa_id = ?", guia)).longValue());
            assertEquals("58015435", valorNativo(em, "select cep from {h-schema}pessoa where id = ?", guia));
            return null;
        });
    }

    @Test
    void consultaPolimorficaDevolveOTipoConcreto() {
        Long pesquisador = persistir(criarPesquisador(cpf(), email(), registro(), new BigDecimal("180.00")));
        Long guia = persistir(criarGuia(cpf(), email()));

        transacao(em -> {
            Pesquisador encontrado = assertInstanceOf(Pesquisador.class, em.find(Pessoa.class, pesquisador));
            assertEquals(Titulacao.DOUTORADO, encontrado.getTitulacao());
            assertEquals(0, new BigDecimal("180.00").compareTo(encontrado.getValorDiarioBolsa()));

            List<Pessoa> guias = em.createQuery(
                    "select p from Pessoa p where type(p) = GuiaEspeleologia and p.id = :id", Pessoa.class)
                    .setParameter("id", guia)
                    .getResultList();
            assertEquals(1, guias.size());
            assertEquals(NivelCertificacao.INTERMEDIARIO,
                    assertInstanceOf(GuiaEspeleologia.class, guias.getFirst()).getNivelCertificacao());
            return null;
        });
    }

    @Test
    void referenciaPelaRaizEIgualAoSubtipoCarregado() {
        Long id = persistir(criarPesquisador(cpf(), email(), registro(), BigDecimal.TEN));
        Pesquisador carregado = transacao(em -> em.find(Pesquisador.class, id));

        transacao(em -> {
            Pessoa referencia = em.getReference(Pessoa.class, id);
            assertEquals(referencia, carregado);
            assertEquals(carregado, referencia);
            assertEquals(carregado.hashCode(), referencia.hashCode());
            return null;
        });
    }

    @Test
    void impedeCpfDuplicadoMesmoComFormatacaoDiferente() {
        persistir(criarPessoa("529.982.247-25", email()));

        PersistenceException erro = assertThrows(PersistenceException.class,
                () -> persistir(criarPessoa("52998224725", email())));

        exigirSqlState(erro, "23505");
    }

    @Test
    void impedeEmailDuplicadoIndependenteDeMaiusculas() {
        String email = email();
        persistir(criarPessoa(cpf(), email));

        PersistenceException erro = assertThrows(PersistenceException.class,
                () -> persistir(criarPessoa(cpf(), " " + email.toUpperCase() + " ")));

        exigirSqlState(erro, "23505");
    }

    @Test
    void impedeRegistroInstitucionalDuplicado() {
        String registro = registro();
        persistir(criarPesquisador(cpf(), email(), registro, BigDecimal.TEN));

        PersistenceException erro = assertThrows(PersistenceException.class,
                () -> persistir(criarPesquisador(cpf(), email(), registro, BigDecimal.TEN)));

        exigirSqlState(erro, "23505");
    }

    @Test
    void impedeBolsaNegativaNoPostgres() {
        PersistenceException erro = assertThrows(PersistenceException.class,
                () -> persistir(criarPesquisador(cpf(), email(), registro(), new BigDecimal("-0.01"))));

        exigirSqlState(erro, "23514");
    }

    @Test
    void usaTiposNativosDoPostgres() throws SQLException {
        assertEquals("boolean", tipoDaColuna("pessoa", "ativo"));
        assertEquals("date", tipoDaColuna("pessoa", "data_nascimento"));
        assertEquals("character varying", tipoDaColuna("pessoa", "tipo_pessoa"));
        assertEquals("numeric", tipoDaColuna("pesquisador", "valor_diario_bolsa"));
        assertEquals("character varying", tipoDaColuna("pesquisador", "titulacao"));
        assertEquals("date", tipoDaColuna("guia_espeleologia", "validade_certificacao"));
        assertNotNull(tipoDaColuna("pessoa", "cep"), "endereco deve ficar na tabela pessoa");
    }

    private Long persistir(Pessoa pessoa) {
        return transacao(em -> {
            em.persist(pessoa);
            return pessoa.getId();
        });
    }

    private Object valorNativo(EntityManager em, String sql, Long id) {
        return em.createNativeQuery(sql).setParameter(1, id).getSingleResult();
    }

    private Pessoa criarPessoa(String cpf, String email) {
        return new Pessoa("Apoio Técnico", cpf, LocalDate.of(1990, 1, 15), email, "83999990000", endereco());
    }

    private Pesquisador criarPesquisador(String cpf, String email, String registro, BigDecimal bolsa) {
        return new Pesquisador("Ana Beatriz Lima", cpf, LocalDate.of(1985, 3, 14), email, "83999990001",
                endereco(), registro, "Bioespeleologia", Titulacao.DOUTORADO, bolsa);
    }

    private GuiaEspeleologia criarGuia(String cpf, String email) {
        return new GuiaEspeleologia("Maria Eduarda Souto", cpf, LocalDate.of(1988, 6, 21), email, "83999990002",
                endereco(), "SBE-" + UUID.randomUUID().toString().substring(0, 8), NivelCertificacao.INTERMEDIARIO,
                LocalDate.of(2027, 6, 30));
    }

    private Endereco endereco() {
        return new Endereco("Av. Primeiro de Maio", "720", null, "Jaguaribe",
                "João Pessoa", UnidadeFederativa.PB, "58015-435");
    }

    private String cpf() {
        return String.format("%011d", ThreadLocalRandom.current().nextLong(1L, 99_999_999_999L));
    }

    private String email() {
        return "teste-" + UUID.randomUUID() + "@turmalina.org";
    }

    private String registro() {
        return "REG-" + UUID.randomUUID().toString().substring(0, 20);
    }
}
