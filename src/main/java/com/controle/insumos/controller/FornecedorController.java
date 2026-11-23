package com.controle.insumos.controller;

import com.esquadralum.insumos.dto.Dtos.FornecedorRequest;
import com.esquadralum.insumos.dto.Dtos.FornecedorResponse;
import com.esquadralum.insumos.service.FornecedorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fornecedores")
public class FornecedorController {
    private final FornecedorService service;

    public FornecedorController(FornecedorService service) { this.service = service; }

    @GetMapping
    public List<FornecedorResponse> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public FornecedorResponse buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FornecedorResponse criar(@Valid @RequestBody FornecedorRequest req) { return service.criar(req); }

    @PutMapping("/{id}")
    public FornecedorResponse atualizar(@PathVariable Long id, @Valid @RequestBody FornecedorRequest req) {
        return service.atualizar(id, req);
    }
}
