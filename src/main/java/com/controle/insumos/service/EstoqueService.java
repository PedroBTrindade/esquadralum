package com.controle.insumos.service;

import com.esquadralum.insumos.model.Embalagem;
import com.esquadralum.insumos.model.Insumo;
import com.esquadralum.insumos.model.SituacaoEmbalagem;
import com.esquadralum.insumos.repository.EmbalagemRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Regras de calculo: consolidado, validade, situacao do insumo e FEFO. Sem estado. */
@Service
public class EstoqueService {

    public static final String VENCIDA = "VENCIDA";
    public static final String PROXIMA = "PROXIMA_VENCIMENTO";
    public static final String NO_PRAZO = "NO_PRAZO";

    private final EmbalagemRepository embalagemRepository;
    private final int diasAlertaValidade;

    public EstoqueService(EmbalagemRepository embalagemRepository,
                          @Value("${app.dias-alerta-validade:30}") int diasAlertaValidade) {
        this.embalagemRepository = embalagemRepository;
        this.diasAlertaValidade = diasAlertaValidade;
    }

    public static BigDecimal normalizar(BigDecimal v) {
        return v.setScale(3, RoundingMode.HALF_UP);
    }

    public LocalDate hoje() { return LocalDate.now(); }

    public boolean vencida(Embalagem e) { return e.getValidade().isBefore(hoje()); }

    public String situacaoValidade(Embalagem e) {
        LocalDate hoje = hoje();
        if (e.getValidade().isBefore(hoje)) return VENCIDA;
        if (!e.getValidade().isAfter(hoje.plusDays(diasAlertaValidade))) return PROXIMA;
        return NO_PRAZO;
    }

    /** Pode ser retirada: disponivel, com saldo e dentro da validade. */
    public boolean utilizavel(Embalagem e) {
        return e.getSituacao() == SituacaoEmbalagem.DISPONIVEL
                && e.getSaldo().signum() > 0
                && !vencida(e);
    }

    public BigDecimal consolidado(Insumo insumo) {
        BigDecimal soma = embalagemRepository.somaDisponivel(insumo.getId(), hoje());
        return soma == null ? BigDecimal.ZERO : soma;
    }

    public String situacaoInsumo(Insumo insumo, BigDecimal consolidado) {
        if (consolidado.signum() <= 0) return "SEM_ESTOQUE";
        if (consolidado.compareTo(insumo.getEstoqueMinimo()) <= 0) return "BAIXO";
        return "DISPONIVEL";
    }

    /** FEFO: embalagem utilizavel com a validade mais proxima. */
    public Optional<Embalagem> primeiraFefo(Long insumoId) {
        return embalagemRepository.findByInsumoIdOrderByValidadeAscIdAsc(insumoId).stream()
                .filter(this::utilizavel)
                .findFirst();
    }
}
