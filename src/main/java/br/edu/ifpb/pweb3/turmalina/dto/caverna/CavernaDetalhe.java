package br.edu.ifpb.pweb3.turmalina.dto.caverna;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CavernaDetalhe(
        Long id,
        String nomeOficial,
        String codigoCadastroAmbiental,
        String municipio,
        UnidadeFederativa uf,
        LocalizacaoDados localizacao,
        BigDecimal altitude,
        BigDecimal extensaoConhecida,
        LocalDate dataUltimaInspecao,
        boolean acessoPermitido) {
}
