package com.controle.insumos.controller;

import com.esquadralum.insumos.dto.Dtos.EmbalagemResponse;
import com.esquadralum.insumos.dto.Dtos.RecebimentoRequest;
import com.esquadralum.insumos.service.EmbalagemService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class EmbalagemController {
    private final EmbalagemService service;

    public EmbalagemController(EmbalagemService service) { this.service = service; }

    /** Codigo lido do QR ou digitado: EMB-0001. Inclui historico e selo FEFO. */
    @GetMapping("/embalagens/{codigo}")
    public EmbalagemResponse consultar(@PathVariable String codigo) { return service.consultar(codigo); }

    @GetMapping("/embalagens")
    public List<EmbalagemResponse> listar(@RequestParam(required = false) Long insumoId,
                                          @RequestParam(required = false) Long fornecedorId,
                                          @RequestParam(required = false) String situacaoValidade) {
        return service.listar(insumoId, fornecedorId, situacaoValidade);
    }

    @PostMapping("/recebimentos")
    @ResponseStatus(HttpStatus.CREATED)
    public List<EmbalagemResponse> receber(@Valid @RequestBody RecebimentoRequest req) {
        return service.receber(req);
    }
}
