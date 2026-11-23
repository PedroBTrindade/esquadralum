package com.controle.insumos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "fornecedor")
public class Fornecedor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nome;
    @Column(length = 200)
    private String contato;

    public Fornecedor() {}
    public Fornecedor(String nome, String contato) { this.nome = nome; this.contato = contato; }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getContato() { return contato; }
    public void setContato(String contato) { this.contato = contato; }
}
