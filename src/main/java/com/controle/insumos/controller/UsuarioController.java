package com.controle.insumos.controller;

import com.esquadralum.insumos.dto.Dtos.UsuarioResponse;
import com.esquadralum.insumos.repository.UsuarioRepository;
import com.esquadralum.insumos.service.Mapeador;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsuarioController {
    private final UsuarioRepository repository;
    private final Mapeador mapeador;

    public UsuarioController(UsuarioRepository repository, Mapeador mapeador) {
        this.repository = repository;
        this.mapeador = mapeador;
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listar() {
        return repository.findAll().stream().map(mapeador::usuario).toList();
    }
}
