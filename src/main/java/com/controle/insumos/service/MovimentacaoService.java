package com.controle.insumos.service;

import com.esquadralum.insumos.dto.Dtos.*;
import com.esquadralum.insumos.exception.NaoEncontradoException;
import com.esquadralum.insumos.exception.RegraNegocioException;
import com.esquadralum.insumos.model.*;
import com.esquadralum.insumos.repository.MovimentacaoRepository;
import com.esquadralum.insumos.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimentacaoService {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MovimentacaoRepository movimentacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmbalagemService embalagemService;
    private final EstoqueService estoque;
    private final Mapeador mapeador;

    public MovimentacaoService(MovimentacaoRepository movimentacaoRepository, UsuarioRepository usuarioRepository,
                               EmbalagemService embalagemService, EstoqueService estoque, Mapeador mapeador) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.embalagemService = embalagemService;
        this.estoque = estoque;
        this.mapeador = mapeador;
    }

    /** RF06: saldo da embalagem e movimentacao sao gravados na mesma transacao. */
    @Transactional
    public SaidaResponse registrarSaida(SaidaRequest req) {
        Usuario usuario = usuario(req.usuarioId());
        Embalagem e = embalagemService.buscarEntidade(req.codigoEmbalagem());
        BigDecimal q = EstoqueService.normalizar(req.quantidade());
        if (q.signum() <= 0) throw new RegraNegocioException("A quantidade deve ser maior que zero.");

        if (e.getSituacao() == SituacaoEmbalagem.ESGOTADA) {
            throw new RegraNegocioException("A embalagem " + e.getCodigo() + " está esgotada.");
        }
        if (e.getSituacao() == SituacaoEmbalagem.BLOQUEADA) {
            throw new RegraNegocioException("A embalagem " + e.getCodigo() + " está bloqueada.");
        }
        if (estoque.vencida(e)) {
            throw new RegraNegocioException("A embalagem " + e.getCodigo() + " venceu em "
                    + e.getValidade().format(DATA) + ". A retirada está bloqueada.");
        }
        if (q.compareTo(e.getSaldo()) > 0) {
            throw new RegraNegocioException("Quantidade maior que o saldo da embalagem (saldo: "
                    + e.getSaldo().stripTrailingZeros().toPlainString() + " "
                    + e.getInsumo().getUnidadeMedida() + ").");
        }

        // FEFO: calculado antes de alterar o saldo
        Optional<Embalagem> primeira = estoque.primeiraFefo(e.getInsumo().getId());
        String aviso = primeira.filter(p -> !p.getId().equals(e.getId()))
                .map(p -> "Há uma embalagem com validade mais próxima: " + p.getCodigo()
                        + " (vence em " + p.getValidade().format(DATA) + "). Prefira usá-la primeiro.")
                .orElse(null);

        BigDecimal anterior = e.getSaldo();
        BigDecimal posterior = anterior.subtract(q);
        e.setSaldo(posterior);
        if (posterior.signum() == 0) e.setSituacao(SituacaoEmbalagem.ESGOTADA);

        Movimentacao m = movimentacaoRepository.save(new Movimentacao(TipoMovimentacao.SAIDA, e, q, anterior,
                posterior,
                req.pesoInformado() == null ? null : EstoqueService.normalizar(req.pesoInformado()),
                usuario, req.motivo().trim()));
        return new SaidaResponse(mapeador.movimentacao(m), posterior, e.getSituacao().name(), aviso);
    }

    /** Ajuste controlado: nunca altera movimentacoes antigas, so cria uma nova. */
    @Transactional
    public MovimentacaoResponse registrarAjuste(AjusteRequest req) {
        Usuario usuario = usuario(req.usuarioId());
        Embalagem e = embalagemService.buscarEntidade(req.codigoEmbalagem());
        BigDecimal novo = EstoqueService.normalizar(req.novoSaldo());
        if (novo.signum() < 0) throw new RegraNegocioException("O novo saldo não pode ser negativo.");

        BigDecimal anterior = e.getSaldo();
        BigDecimal diferenca = novo.subtract(anterior).abs();
        if (diferenca.signum() == 0) {
            throw new RegraNegocioException("O novo saldo é igual ao saldo atual. Nada a ajustar.");
        }
        e.setSaldo(novo);
        if (novo.signum() == 0) {
            e.setSituacao(SituacaoEmbalagem.ESGOTADA);
        } else if (e.getSituacao() == SituacaoEmbalagem.ESGOTADA) {
            e.setSituacao(SituacaoEmbalagem.DISPONIVEL);
        }
        Movimentacao m = movimentacaoRepository.save(new Movimentacao(TipoMovimentacao.AJUSTE, e, diferenca,
                anterior, novo, null, usuario, "Ajuste: " + req.justificativa().trim()));
        return mapeador.movimentacao(m);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoResponse> listar(Long insumoId, String codigo, LocalDate de, LocalDate ate) {
        String cod = codigo == null || codigo.isBlank() ? null : codigo.trim().toUpperCase();
        LocalDateTime ini = de == null ? null : de.atStartOfDay();
        LocalDateTime fim = ate == null ? null : ate.plusDays(1).atStartOfDay();
        return movimentacaoRepository.findAllByOrderByDataHoraDescIdDesc().stream()
                .filter(m -> insumoId == null || m.getEmbalagem().getInsumo().getId().equals(insumoId))
                .filter(m -> cod == null || m.getEmbalagem().getCodigo().equals(cod))
                .filter(m -> ini == null || !m.getDataHora().isBefore(ini))
                .filter(m -> fim == null || m.getDataHora().isBefore(fim))
                .map(mapeador::movimentacao)
                .toList();
    }

    private Usuario usuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Responsável não encontrado."));
    }
}
