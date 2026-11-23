package com.controle.insumos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nome;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilUsuario perfil;

    public Usuario() {}
    public Usuario(String nome, PerfilUsuario perfil) { this.nome = nome; this.perfil = perfil; }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public PerfilUsuario getPerfil() { return perfil; }
}
