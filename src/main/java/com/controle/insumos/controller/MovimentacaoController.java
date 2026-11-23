package com.controle.insumos.controller;

import com.esquadralum.insumos.dto.Dtos.*;
import com.esquadralum.insumos.service.MovimentacaoService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class MovimentacaoController {
    private final MovimentacaoService service;

    public MovimentacaoController(MovimentacaoService service) { this.service = service; }

    @PostMapping("/saidas")
    @ResponseStatus(HttpStatus.CREATED)
    public SaidaResponse saida(@Valid @RequestBody SaidaRequest req) { return service.registrarSaida(req); }

    @PostMapping("/ajustes")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoResponse ajuste(@Valid @RequestBody AjusteRequest req) {
        return service.registrarAjuste(req);
    }

    @GetMapping("/movimentacoes")
    public List<MovimentacaoResponse> listar(
            @RequestParam(required = false) Long insumoId,
            @RequestParam(required = false) String codigoEmbalagem,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return service.listar(insumoId, codigoEmbalagem, de, ate);
    }
}
