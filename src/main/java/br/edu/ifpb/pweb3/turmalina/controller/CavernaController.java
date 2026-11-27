package br.edu.ifpb.pweb3.turmalina.controller;

import br.edu.ifpb.pweb3.turmalina.dto.caverna.CavernaDetalhe;
import br.edu.ifpb.pweb3.turmalina.dto.caverna.CavernaEntrada;
import br.edu.ifpb.pweb3.turmalina.dto.caverna.CavernaResumo;
import br.edu.ifpb.pweb3.turmalina.service.CavernaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/cavernas")
public class CavernaController {

    private final CavernaService servico;

    public CavernaController(CavernaService servico) {
        this.servico = servico;
    }

    @PostMapping
    public ResponseEntity<CavernaDetalhe> criar(@Valid @RequestBody CavernaEntrada entrada) {
        CavernaDetalhe criada = servico.criar(entrada);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.id())
                .toUri();
        return ResponseEntity.created(local).body(criada);
    }

    @GetMapping
    public Page<CavernaResumo> listar(@SortDefault("nomeOficial") Pageable paginacao) {
        return servico.listar(paginacao);
    }

    @GetMapping("/{id}")
    public CavernaDetalhe buscar(@PathVariable Long id) {
        return servico.buscar(id);
    }

    @PutMapping("/{id}")
    public CavernaDetalhe atualizar(@PathVariable Long id, @Valid @RequestBody CavernaEntrada entrada) {
        return servico.atualizar(id, entrada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        servico.remover(id);
        return ResponseEntity.noContent().build();
    }
}
