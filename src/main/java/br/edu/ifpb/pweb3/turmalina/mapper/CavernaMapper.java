package br.edu.ifpb.pweb3.turmalina.mapper;

import br.edu.ifpb.pweb3.turmalina.dominio.Caverna;
import br.edu.ifpb.pweb3.turmalina.dto.caverna.CavernaDetalhe;
import br.edu.ifpb.pweb3.turmalina.dto.caverna.CavernaResumo;
import org.mapstruct.Mapper;

@Mapper(config = MapeamentoConfig.class)
public interface CavernaMapper {

    CavernaDetalhe paraDetalhe(Caverna caverna);

    CavernaResumo paraResumo(Caverna caverna);
}
