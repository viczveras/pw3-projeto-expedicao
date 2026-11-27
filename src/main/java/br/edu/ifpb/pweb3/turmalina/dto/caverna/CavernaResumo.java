package br.edu.ifpb.pweb3.turmalina.dto.caverna;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;

public record CavernaResumo(
        Long id,
        String nomeOficial,
        String codigoCadastroAmbiental,
        String municipio,
        UnidadeFederativa uf,
        boolean acessoPermitido) {
}
