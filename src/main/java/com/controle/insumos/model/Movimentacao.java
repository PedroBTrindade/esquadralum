package com.controle.insumos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Registro imutavel: so construtor e getters, e colunas com updatable=false. */
@Entity
@Table(name = "movimentacao")
public class Movimentacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private TipoMovimentacao tipo;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "embalagem_id", nullable = false, updatable = false)
    private Embalagem embalagem;
    @Column(nullable = false, updatable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;
    @Column(nullable = false, updatable = false, precision = 12, scale = 3)
    private BigDecimal saldoAnterior;
    @Column(nullable = false, updatable = false, precision = 12, scale = 3)
    private BigDecimal saldoPosterior;
    @Column(updatable = false, precision = 12, scale = 3)
    private BigDecimal pesoInformado;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "responsavel_id", nullable = false, updatable = false)
    private Usuario responsavel;
    @Column(nullable = false, updatable = false, length = 300)
    private String motivo;
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHora;

    protected Movimentacao() {}

    public Movimentacao(TipoMovimentacao tipo, Embalagem embalagem, BigDecimal quantidade,
                        BigDecimal saldoAnterior, BigDecimal saldoPosterior, BigDecimal pesoInformado,
                        Usuario responsavel, String motivo) {
        this.tipo = tipo;
        this.embalagem = embalagem;
        this.quantidade = quantidade;
        this.saldoAnterior = saldoAnterior;
        this.saldoPosterior = saldoPosterior;
        this.pesoInformado = pesoInformado;
        this.responsavel = responsavel;
        this.motivo = motivo;
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public TipoMovimentacao getTipo() { return tipo; }
    public Embalagem getEmbalagem() { return embalagem; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getSaldoAnterior() { return saldoAnterior; }
    public BigDecimal getSaldoPosterior() { return saldoPosterior; }
    public BigDecimal getPesoInformado() { return pesoInformado; }
    public Usuario getResponsavel() { return responsavel; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getDataHora() { return dataHora; }
}
