package br.edu.ifpb.pweb3.turmalina.app;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class Inicializacao {

    private Inicializacao() {
    }

    public static void main(String[] args) {
        try (EntityManagerFactory fabrica = Persistence.createEntityManagerFactory(
                Configuracao.UNIDADE_PERSISTENCIA, Configuracao.propriedadesDoAmbiente());
             EntityManager em = fabrica.createEntityManager()) {
            Long cavernas = em.createQuery("select count(c) from Caverna c", Long.class).getSingleResult();
            Long setores = em.createQuery("select count(s) from Setor s", Long.class).getSingleResult();
            System.out.println("TurmalinaPB: conexao e mapeamentos inicializados.");
            System.out.printf("Cadastros existentes: %d caverna(s), %d setor(es).%n", cavernas, setores);
        }
    }
}
