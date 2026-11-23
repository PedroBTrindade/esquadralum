package com.controle.insumos.service;

import com.esquadralum.insumos.dto.Dtos.*;
import com.esquadralum.insumos.model.*;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

/** Converte entidades em DTOs. Deve ser chamado dentro da transacao do servico. */
@Component
public class Mapeador {
    private final EstoqueService estoque;

    public Mapeador(EstoqueService estoque) { this.estoque = estoque; }

    public InsumoResponse insumo(Insumo i) {
        BigDecimal c = estoque.consolidado(i);
        return new InsumoResponse(i.getId(), i.getCodigo(), i.getNome(), i.getCategoria(), i.getDescricao(),
                i.getUnidadeMedida(), i.getEstoqueMinimo(), i.isAtivo(), c, estoque.situacaoInsumo(i, c));
    }

    public InsumoResumo resumo(Insumo i) {
        return new InsumoResumo(i.getId(), i.getCodigo(), i.getNome());
    }

    public FornecedorResponse fornecedor(Fornecedor f, List<Insumo> insumos) {
        return new FornecedorResponse(f.getId(), f.getNome(), f.getContato(),
                insumos == null ? null : insumos.stream().map(this::resumo).toList());
    }

    public UsuarioResponse usuario(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getPerfil().name());
    }

    public MovimentacaoResponse movimentacao(Movimentacao m) {
        Embalagem e = m.getEmbalagem();
        return new MovimentacaoResponse(m.getId(), m.getTipo().name(), e.getCodigo(), e.getInsumo().getNome(),
                e.getInsumo().getUnidadeMedida(), m.getQuantidade(), m.getSaldoAnterior(), m.getSaldoPosterior(),
                m.getPesoInformado(), m.getResponsavel().getNome(), m.getMotivo(), m.getDataHora());
    }

    public EmbalagemResponse embalagem(Embalagem e, boolean usarPrimeiro, List<Movimentacao> historico) {
        Insumo i = e.getInsumo();
        return new EmbalagemResponse(e.getId(), e.getCodigo(), i.getId(), i.getCodigo(), i.getNome(),
                i.getUnidadeMedida(), e.getFornecedor().getId(), e.getFornecedor().getNome(),
                e.getDataRecebimento(), e.getValidade(), e.getQuantidadeOriginal(), e.getSaldo(),
                e.getLocalArmazenamento(), e.getLoteFabricante(), e.getSituacao().name(),
                estoque.situacaoValidade(e), usarPrimeiro,
                historico == null ? null : historico.stream().map(this::movimentacao).toList());
    }
}
