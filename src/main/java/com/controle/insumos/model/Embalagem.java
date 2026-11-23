package com.controle.insumos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Unidade fisica recebida. O codigo e o conteudo do QR Code. */
@Entity
@Table(name = "embalagem", indexes = @Index(name = "idx_embalagem_validade", columnList = "validade"))
public class Embalagem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;
    @Column(nullable = false)
    private LocalDate dataRecebimento;
    @Column(nullable = false)
    private LocalDate validade;
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidadeOriginal;
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal saldo;
    @Column(nullable = false, length = 80)
    private String localArmazenamento;
    @Column(length = 60)
    private String loteFabricante;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoEmbalagem situacao = SituacaoEmbalagem.DISPONIVEL;
    /** Controle otimista: duas retiradas simultaneas da mesma embalagem nao se sobrepoem. */
    @Version
    private Long versao;

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Insumo getInsumo() { return insumo; }
    public void setInsumo(Insumo insumo) { this.insumo = insumo; }
    public Fornecedor getFornecedor() { return fornecedor; }
    public void setFornecedor(Fornecedor fornecedor) { this.fornecedor = fornecedor; }
    public LocalDate getDataRecebimento() { return dataRecebimento; }
    public void setDataRecebimento(LocalDate d) { this.dataRecebimento = d; }
    public LocalDate getValidade() { return validade; }
    public void setValidade(LocalDate validade) { this.validade = validade; }
    public BigDecimal getQuantidadeOriginal() { return quantidadeOriginal; }
    public void setQuantidadeOriginal(BigDecimal q) { this.quantidadeOriginal = q; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public String getLocalArmazenamento() { return localArmazenamento; }
    public void setLocalArmazenamento(String l) { this.localArmazenamento = l; }
    public String getLoteFabricante() { return loteFabricante; }
    public void setLoteFabricante(String l) { this.loteFabricante = l; }
    public SituacaoEmbalagem getSituacao() { return situacao; }
    public void setSituacao(SituacaoEmbalagem situacao) { this.situacao = situacao; }
    public Long getVersao() { return versao; }
}
