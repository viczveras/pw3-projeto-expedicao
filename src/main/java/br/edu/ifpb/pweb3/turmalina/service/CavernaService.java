package br.edu.ifpb.pweb3.turmalina.service;

import br.edu.ifpb.pweb3.turmalina.dominio.Caverna;
import br.edu.ifpb.pweb3.turmalina.dominio.valor.Localizacao;
import br.edu.ifpb.pweb3.turmalina.dto.caverna.CavernaDetalhe;
import br.edu.ifpb.pweb3.turmalina.dto.caverna.CavernaEntrada;
import br.edu.ifpb.pweb3.turmalina.dto.caverna.CavernaResumo;
import br.edu.ifpb.pweb3.turmalina.dto.caverna.LocalizacaoDados;
import br.edu.ifpb.pweb3.turmalina.exception.ConflitoException;
import br.edu.ifpb.pweb3.turmalina.exception.RecursoNaoEncontradoException;
import br.edu.ifpb.pweb3.turmalina.mapper.CavernaMapper;
import br.edu.ifpb.pweb3.turmalina.repository.CavernaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CavernaService {

    private final CavernaRepository repositorio;
    private final CavernaMapper mapper;

    public CavernaService(CavernaRepository repositorio, CavernaMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Transactional
    public CavernaDetalhe criar(CavernaEntrada entrada) {
        if (repositorio.existsByCodigoCadastroAmbiental(entrada.codigoCadastroAmbiental())) {
            throw codigoRepetido(entrada.codigoCadastroAmbiental());
        }
        Caverna caverna = new Caverna(entrada.nomeOficial(), entrada.codigoCadastroAmbiental(),
                entrada.municipio(), entrada.uf(), localizacao(entrada.localizacao()));
        caverna.setAltitude(entrada.altitude());
        caverna.setExtensaoConhecida(entrada.extensaoConhecida());
        return mapper.paraDetalhe(repositorio.save(caverna));
    }

    @Transactional(readOnly = true)
    public Page<CavernaResumo> listar(Pageable paginacao) {
        return repositorio.findAll(paginacao).map(mapper::paraResumo);
    }

    @Transactional(readOnly = true)
    public CavernaDetalhe buscar(Long id) {
        return mapper.paraDetalhe(carregar(id));
    }

    @Transactional
    public CavernaDetalhe atualizar(Long id, CavernaEntrada entrada) {
        Caverna caverna = carregar(id);
        if (repositorio.existsByCodigoCadastroAmbientalAndIdNot(entrada.codigoCadastroAmbiental(), id)) {
            throw codigoRepetido(entrada.codigoCadastroAmbiental());
        }
        caverna.atualizarCadastro(entrada.nomeOficial(), entrada.codigoCadastroAmbiental(),
                entrada.municipio(), entrada.uf());
        caverna.setLocalizacao(localizacao(entrada.localizacao()));
        caverna.setAltitude(entrada.altitude());
        caverna.setExtensaoConhecida(entrada.extensaoConhecida());
        return mapper.paraDetalhe(caverna);
    }

    @Transactional
    public void remover(Long id) {
        Caverna caverna = carregar(id);
        if (repositorio.possuiExpedicao(id)) {
            throw new ConflitoException("A caverna %d tem expedições e não pode ser removida.".formatted(id));
        }
        repositorio.delete(caverna);
    }

    private Caverna carregar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Caverna %d não encontrada.".formatted(id)));
    }

    private static Localizacao localizacao(LocalizacaoDados dados) {
        return new Localizacao(dados.latitude(), dados.longitude(), dados.datum());
    }

    private static ConflitoException codigoRepetido(String codigo) {
        return new ConflitoException("Já existe caverna com o código ambiental %s.".formatted(codigo));
    }
}
