package com.controle.insumos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Produto do catalogo. NAO e uma embalagem fisica. */
@Entity
@Table(name = "insumo")
public class Insumo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 40)
    private String codigo;
    @Column(nullable = false, length = 120)
    private String nome;
    @Column(length = 60)
    private String categoria;
    @Column(length = 500)
    private String descricao;
    @Column(nullable = false, length = 10)
    private String unidadeMedida;
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal estoqueMinimo;
    @Column(nullable = false)
    private boolean ativo = true;

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getUnidadeMedida() { return unidadeMedida; }
    public void setUnidadeMedida(String unidadeMedida) { this.unidadeMedida = unidadeMedida; }
    public BigDecimal getEstoqueMinimo() { return estoqueMinimo; }
    public void setEstoqueMinimo(BigDecimal estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
