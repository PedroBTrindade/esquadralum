package com.controle.insumos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Todos os DTOs (records) agrupados em um unico arquivo para facilitar a leitura. */
public final class Dtos {
    private Dtos() {}

    // ---- Insumo ----
    public record InsumoRequest(
            @NotBlank String codigo,
            @NotBlank String nome,
            String categoria,
            String descricao,
            @NotBlank String unidadeMedida,
            @NotNull @DecimalMin("0") BigDecimal estoqueMinimo) {}

    public record InsumoResponse(Long id, String codigo, String nome, String categoria, String descricao,
                                 String unidadeMedida, BigDecimal estoqueMinimo, boolean ativo,
                                 BigDecimal estoqueConsolidado, String situacao) {}

    public record InsumoResumo(Long id, String codigo, String nome) {}

    // ---- Fornecedor / Usuario ----
    public record FornecedorRequest(@NotBlank String nome, String contato) {}

    public record FornecedorResponse(Long id, String nome, String contato, List<InsumoResumo> insumosFornecidos) {}

    public record UsuarioResponse(Long id, String nome, String perfil) {}

    // ---- Movimentacao / Embalagem ----
    public record MovimentacaoResponse(Long id, String tipo, String codigoEmbalagem, String insumoNome,
                                       String unidadeMedida, BigDecimal quantidade, BigDecimal saldoAnterior,
                                       BigDecimal saldoPosterior, BigDecimal pesoInformado, String responsavel,
                                       String motivo, LocalDateTime dataHora) {}

    public record EmbalagemResponse(Long id, String codigo, Long insumoId, String insumoCodigo, String insumoNome,
                                    String unidadeMedida, Long fornecedorId, String fornecedorNome,
                                    LocalDate dataRecebimento, LocalDate validade, BigDecimal quantidadeOriginal,
                                    BigDecimal saldo, String localArmazenamento, String loteFabricante,
                                    String situacao, String situacaoValidade, boolean usarPrimeiro,
                                    List<MovimentacaoResponse> historico) {}

    // ---- Operacoes ----
    public record ItemRecebimento(
            @NotNull LocalDate validade,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal quantidade,
            String loteFabricante) {}

    public record RecebimentoRequest(
            @NotNull Long insumoId,
            @NotNull Long fornecedorId,
            @NotBlank String local,
            @NotNull Long usuarioId,
            @NotEmpty @Valid List<ItemRecebimento> embalagens) {}

    public record SaidaRequest(
            @NotBlank String codigoEmbalagem,
            @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal quantidade,
            @NotBlank String motivo,
            @DecimalMin(value = "0", inclusive = false) BigDecimal pesoInformado,
            @NotNull Long usuarioId) {}

    public record SaidaResponse(MovimentacaoResponse movimentacao, BigDecimal saldoAtual, String situacao,
                                String avisoFefo) {}

    public record AjusteRequest(
            @NotBlank String codigoEmbalagem,
            @NotNull @DecimalMin("0") BigDecimal novoSaldo,
            @NotBlank String justificativa,
            @NotNull Long usuarioId) {}

    // ---- Painel ----
    public record AlertaEstoque(Long insumoId, String codigo, String nome, String unidadeMedida,
                                BigDecimal atual, BigDecimal minimo, String situacao) {}

    public record AlertaValidade(String codigoEmbalagem, Long insumoId, String insumoNome, LocalDate validade,
                                 long diasParaVencer, BigDecimal saldo, String unidadeMedida,
                                 String situacaoValidade) {}

    public record PainelResponse(long cadastrados, long disponiveis, long estoqueBaixo, long semEstoque,
                                 List<AlertaEstoque> alertasEstoque, List<AlertaValidade> alertasValidade,
                                 List<MovimentacaoResponse> ultimasMovimentacoes) {}

    public record ErroResponse(String mensagem) {}
}
