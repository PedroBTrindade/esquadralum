package com.controle.insumos.controller;

import com.esquadralum.insumos.dto.Dtos.PainelResponse;
import com.esquadralum.insumos.service.PainelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PainelController {
    private final PainelService service;

    public PainelController(PainelService service) { this.service = service; }

    @GetMapping("/painel")
    public PainelResponse painel() { return service.montar(); }
}
