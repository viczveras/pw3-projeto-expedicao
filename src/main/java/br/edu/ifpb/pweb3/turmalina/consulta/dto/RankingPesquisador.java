package br.edu.ifpb.pweb3.turmalina.consulta.dto;

public record RankingPesquisador(
        Long pesquisadorId,
        String nome,
        Long totalAmostras
) {}