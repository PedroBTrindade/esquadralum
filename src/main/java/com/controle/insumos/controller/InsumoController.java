package com.controle.insumos.controller;

import com.esquadralum.insumos.dto.Dtos.InsumoRequest;
import com.esquadralum.insumos.dto.Dtos.InsumoResponse;
import com.esquadralum.insumos.service.InsumoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/insumos")
public class InsumoController {
    private final InsumoService service;

    public InsumoController(InsumoService service) { this.service = service; }

    @GetMapping
    public List<InsumoResponse> listar(@RequestParam(required = false) String busca,
                                       @RequestParam(required = false) String categoria,
                                       @RequestParam(required = false) String situacao,
                                       @RequestParam(defaultValue = "false") boolean todos) {
        return service.listar(busca, categoria, situacao, todos);
    }

    @GetMapping("/{id}")
    public InsumoResponse buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InsumoResponse criar(@Valid @RequestBody InsumoRequest req) { return service.criar(req); }

    @PutMapping("/{id}")
    public InsumoResponse atualizar(@PathVariable Long id, @Valid @RequestBody InsumoRequest req) {
        return service.atualizar(id, req);
    }

    /** DELETE desativa o insumo; o historico e mantido (RF04, RN09). */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) { service.desativar(id); }
}
