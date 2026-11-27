package br.edu.ifpb.pweb3.turmalina.dto.caverna;

import br.edu.ifpb.pweb3.turmalina.dominio.enums.UnidadeFederativa;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CavernaEntrada(
        @NotBlank @Size(max = 150) String nomeOficial,
        @NotBlank @Size(max = 30) String codigoCadastroAmbiental,
        @NotBlank @Size(max = 100) String municipio,
        @NotNull UnidadeFederativa uf,
        @NotNull @Valid LocalizacaoDados localizacao,
        @Digits(integer = 5, fraction = 2) BigDecimal altitude,
        @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal extensaoConhecida) {
}
