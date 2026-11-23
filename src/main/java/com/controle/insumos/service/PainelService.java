package com.controle.insumos.service;

import com.esquadralum.insumos.dto.Dtos.*;
import com.esquadralum.insumos.model.Embalagem;
import com.esquadralum.insumos.model.Insumo;
import com.esquadralum.insumos.model.SituacaoEmbalagem;
import com.esquadralum.insumos.repository.EmbalagemRepository;
import com.esquadralum.insumos.repository.InsumoRepository;
import com.esquadralum.insumos.repository.MovimentacaoRepository;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PainelService {
    private final InsumoRepository insumoRepository;
    private final EmbalagemRepository embalagemRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final EstoqueService estoque;
    private final Mapeador mapeador;

    public PainelService(InsumoRepository insumoRepository, EmbalagemRepository embalagemRepository,
                         MovimentacaoRepository movimentacaoRepository, EstoqueService estoque, Mapeador mapeador) {
        this.insumoRepository = insumoRepository;
        this.embalagemRepository = embalagemRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.estoque = estoque;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public PainelResponse montar() {
        long cadastrados = 0, disponiveis = 0, baixo = 0, sem = 0;
        List<AlertaEstoque> alertasEstoque = new ArrayList<>();

        for (Insumo i : insumoRepository.findAllByOrderByNomeAsc()) {
            if (!i.isAtivo()) continue;
            cadastrados++;
            BigDecimal c = estoque.consolidado(i);
            String sit = estoque.situacaoInsumo(i, c);
            switch (sit) {
                case "DISPONIVEL" -> disponiveis++;
                case "BAIXO" -> baixo++;
                default -> sem++;
            }
            if (!sit.equals("DISPONIVEL")) {
                alertasEstoque.add(new AlertaEstoque(i.getId(), i.getCodigo(), i.getNome(), i.getUnidadeMedida(),
                        c, i.getEstoqueMinimo(), sit));
            }
        }

        // Alertas de validade: separados dos de estoque. So embalagens ainda com saldo.
        List<AlertaValidade> alertasValidade = new ArrayList<>();
        for (Embalagem e : embalagemRepository.findAllByOrderByValidadeAscIdAsc()) {
            if (e.getSituacao() != SituacaoEmbalagem.DISPONIVEL || e.getSaldo().signum() <= 0) continue;
            String sv = estoque.situacaoValidade(e);
            if (sv.equals(EstoqueService.NO_PRAZO)) continue;
            alertasValidade.add(new AlertaValidade(e.getCodigo(), e.getInsumo().getId(), e.getInsumo().getNome(),
                    e.getValidade(), ChronoUnit.DAYS.between(estoque.hoje(), e.getValidade()), e.getSaldo(),
                    e.getInsumo().getUnidadeMedida(), sv));
        }

        List<MovimentacaoResponse> ultimas = movimentacaoRepository.findTop10ByOrderByDataHoraDescIdDesc()
                .stream().map(mapeador::movimentacao).toList();
        return new PainelResponse(cadastrados, disponiveis, baixo, sem, alertasEstoque, alertasValidade, ultimas);
    }
}
