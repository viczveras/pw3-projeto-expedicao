package br.edu.ifpb.pweb3.turmalina.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Configuração comum dos mappers MapStruct: viram beans do Spring e falham na compilação
 * se algum campo do DTO de saída ficar sem mapeamento.
 * Uso: {@code @Mapper(config = MapeamentoConfig.class)}.
 */
@MapperConfig(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MapeamentoConfig {
}
