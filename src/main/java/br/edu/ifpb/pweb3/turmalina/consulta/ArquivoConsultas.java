package br.edu.ifpb.pweb3.turmalina.consulta;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.SituacaoAutorizacao;
import jakarta.persistence.EntityManager;

import java.util.Objects;
import java.util.Optional;

public class ArquivoConsultas {

    private final EntityManager em;

    public ArquivoConsultas(EntityManager em) {
        this.em = Objects.requireNonNull(em);
    }

    public Optional<byte[]> mapaDeRota(Long expedicaoId) {
        return em.createNamedQuery("PlanoSeguranca.mapaRotaPorExpedicao", byte[].class)
                .setParameter("expedicaoId", expedicaoId)
                .getResultStream()
                .filter(Objects::nonNull)
                .findFirst();
    }

    public Optional<byte[]> pdfAutorizacaoVigente(Long expedicaoId) {
        return em.createNamedQuery("AutorizacaoAmbiental.pdfPorExpedicaoESituacao", byte[].class)
                .setParameter("expedicaoId", expedicaoId)
                .setParameter("situacao", SituacaoAutorizacao.VIGENTE)
                .getResultStream()
                .findFirst();
    }

    public Optional<byte[]> pdfAutorizacao(Long autorizacaoId) {
        return em.createNamedQuery("AutorizacaoAmbiental.pdfPorId", byte[].class)
                .setParameter("autorizacaoId", autorizacaoId)
                .getResultStream()
                .findFirst();
    }

    public Optional<byte[]> arquivoRelatorioFinal(Long expedicaoId) {
        return em.createNamedQuery("RelatorioFinal.arquivoPorExpedicao", byte[].class)
                .setParameter("expedicaoId", expedicaoId)
                .getResultStream()
                .findFirst();
    }

    public Optional<byte[]> fotografiaDaAmostra(Long amostraId) {
        return em.createNamedQuery("Amostra.fotografiaPorId", byte[].class)
                .setParameter("amostraId", amostraId)
                .getResultStream()
                .filter(Objects::nonNull)
                .findFirst();
    }
}
