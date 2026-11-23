package com.controle.insumos.service;

import com.esquadralum.insumos.dto.Dtos.*;
import com.esquadralum.insumos.exception.NaoEncontradoException;
import com.esquadralum.insumos.exception.RegraNegocioException;
import com.esquadralum.insumos.model.*;
import com.esquadralum.insumos.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmbalagemService {
    private final EmbalagemRepository embalagemRepository;
    private final InsumoRepository insumoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final EstoqueService estoque;
    private final Mapeador mapeador;

    public EmbalagemService(EmbalagemRepository embalagemRepository, InsumoRepository insumoRepository,
                            FornecedorRepository fornecedorRepository, UsuarioRepository usuarioRepository,
                            MovimentacaoRepository movimentacaoRepository, EstoqueService estoque,
                            Mapeador mapeador) {
        this.embalagemRepository = embalagemRepository;
        this.insumoRepository = insumoRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.usuarioRepository = usuarioRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.estoque = estoque;
        this.mapeador = mapeador;
    }

    /** Busca a entidade pelo codigo (EMB-0001). Usado tambem por MovimentacaoService. */
    @Transactional(readOnly = true)
    public Embalagem buscarEntidade(String codigo) {
        String c = codigo == null ? "" : codigo.trim().toUpperCase();
        return embalagemRepository.findByCodigo(c).orElseThrow(() -> new NaoEncontradoException(
                "Embalagem não encontrada para o código \"" + c + "\". Confira a etiqueta e tente novamente."));
    }

    @Transactional(readOnly = true)
    public EmbalagemResponse consultar(String codigo) {
        Embalagem e = buscarEntidade(codigo);
        boolean primeiro = estoque.primeiraFefo(e.getInsumo().getId())
                .map(p -> p.getId().equals(e.getId())).orElse(false);
        return mapeador.embalagem(e, primeiro,
                movimentacaoRepository.findByEmbalagemIdOrderByDataHoraDescIdDesc(e.getId()));
    }

    @Transactional(readOnly = true)
    public List<EmbalagemResponse> listar(Long insumoId, Long fornecedorId, String situacaoValidade) {
        Map<Long, Long> primeiroPorInsumo = new HashMap<>();
        return embalagemRepository.findAllByOrderByValidadeAscIdAsc().stream()
                .filter(e -> insumoId == null || e.getInsumo().getId().equals(insumoId))
                .filter(e -> fornecedorId == null || e.getFornecedor().getId().equals(fornecedorId))
                .filter(e -> situacaoValidade == null || situacaoValidade.isBlank()
                        || situacaoValidade.equalsIgnoreCase(estoque.situacaoValidade(e)))
                .map(e -> mapeador.embalagem(e, ehPrimeiro(e, primeiroPorInsumo), null))
                .toList();
    }

    private boolean ehPrimeiro(Embalagem e, Map<Long, Long> cache) {
        Long primeiroId = cache.computeIfAbsent(e.getInsumo().getId(),
                id -> estoque.primeiraFefo(id).map(Embalagem::getId).orElse(-1L));
        return primeiroId.equals(e.getId());
    }

    /** Recebimento: cria uma embalagem por item, com codigo proprio, e registra as ENTRADAS. */
    @Transactional
    public List<EmbalagemResponse> receber(RecebimentoRequest req) {
        Insumo insumo = insumoRepository.findById(req.insumoId())
                .orElseThrow(() -> new NaoEncontradoException("Insumo não encontrado."));
        if (!insumo.isAtivo()) {
            throw new RegraNegocioException("Insumo desativado não pode receber novas embalagens.");
        }
        Fornecedor fornecedor = fornecedorRepository.findById(req.fornecedorId())
                .orElseThrow(() -> new NaoEncontradoException("Fornecedor não encontrado."));
        Usuario usuario = usuarioRepository.findById(req.usuarioId())
                .orElseThrow(() -> new NaoEncontradoException("Responsável não encontrado."));

        LocalDate hoje = estoque.hoje();
        List<ItemRecebimento> itens = req.embalagens();
        for (int n = 0; n < itens.size(); n++) {
            if (itens.get(n).validade().isBefore(hoje)) {
                throw new RegraNegocioException("Embalagem " + (n + 1)
                        + ": a validade não pode ser anterior à data de recebimento.");
            }
        }
        List<Embalagem> criadas = new ArrayList<>();
        for (ItemRecebimento item : itens) {
            criadas.add(criarEmbalagem(insumo, fornecedor, req.local().trim(), item.validade(),
                    item.quantidade(), item.loteFabricante(), usuario, hoje));
        }
        Map<Long, Long> cache = new HashMap<>();
        return criadas.stream().map(e -> mapeador.embalagem(e, ehPrimeiro(e, cache), null)).toList();
    }

    /**
     * Cria a embalagem e a movimentacao de ENTRADA. Sem validacao de data: quem recebe de verdade
     * usa receber(); o DataLoader usa este metodo para criar uma embalagem ja vencida de exemplo.
     */
    @Transactional
    public Embalagem criarEmbalagem(Insumo insumo, Fornecedor fornecedor, String local, LocalDate validade,
                                    BigDecimal quantidade, String lote, Usuario responsavel,
                                    LocalDate dataRecebimento) {
        BigDecimal q = EstoqueService.normalizar(quantidade);
        if (q.signum() <= 0) throw new RegraNegocioException("A quantidade deve ser maior que zero.");

        Embalagem e = new Embalagem();
        e.setCodigo("TMP-" + UUID.randomUUID());   // trocado logo abaixo pelo codigo definitivo
        e.setInsumo(insumo);
        e.setFornecedor(fornecedor);
        e.setDataRecebimento(dataRecebimento);
        e.setValidade(validade);
        e.setQuantidadeOriginal(q);
        e.setSaldo(q);
        e.setLocalArmazenamento(local);
        e.setLoteFabricante(lote == null || lote.isBlank() ? null : lote.trim());
        e.setSituacao(SituacaoEmbalagem.DISPONIVEL);
        e = embalagemRepository.save(e);
        e.setCodigo(String.format("EMB-%04d", e.getId()));

        movimentacaoRepository.save(new Movimentacao(TipoMovimentacao.ENTRADA, e, q, BigDecimal.ZERO.setScale(3),
                q, null, responsavel, "Recebimento de embalagem"));
        return e;
    }
}
