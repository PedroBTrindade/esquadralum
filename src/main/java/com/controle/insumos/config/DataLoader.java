package com.controle.insumos.config;

import com.esquadralum.insumos.model.*;
import com.esquadralum.insumos.repository.*;
import com.esquadralum.insumos.service.EmbalagemService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Dados FICTICIOS para demonstracao. So roda se o banco estiver vazio. */
@Component
public class DataLoader implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;
    private final FornecedorRepository fornecedorRepository;
    private final InsumoRepository insumoRepository;
    private final EmbalagemService embalagemService;

    public DataLoader(UsuarioRepository usuarioRepository, FornecedorRepository fornecedorRepository,
                      InsumoRepository insumoRepository, EmbalagemService embalagemService) {
        this.usuarioRepository = usuarioRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.insumoRepository = insumoRepository;
        this.embalagemService = embalagemService;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) return;

        Usuario joao = usuarioRepository.save(new Usuario("João Silva", PerfilUsuario.COLABORADOR));
        usuarioRepository.save(new Usuario("Ana Souza", PerfilUsuario.GESTOR));
        usuarioRepository.save(new Usuario("Administrador", PerfilUsuario.ADMIN));

        Fornecedor fa = fornecedorRepository.save(new Fornecedor("Fornecedor A", "contato@fornecedora.exemplo"));
        Fornecedor fb = fornecedorRepository.save(new Fornecedor("Fornecedor B", "contato@fornecedorb.exemplo"));

        Insumo silicone = novo("INS-0001", "Silicone Branco", "Adesivos", "un", "2");
        Insumo parafuso = novo("INS-0002", "Parafuso 4,2x32", "Fixação", "un", "50");
        Insumo tomada = novo("INS-0003", "Tomada 10A", "Elétrica", "un", "20");
        Insumo epdm = novo("INS-0004", "Vedação EPDM", "Acessórios", "m", "50");

        LocalDate hoje = LocalDate.now();
        BigDecimal um = BigDecimal.ONE;

        // Silicone: 3 embalagens utilizaveis (uma proxima do vencimento) + 1 vencida
        embalagemService.criarEmbalagem(silicone, fa, "Prateleira B2", hoje.plusDays(15), um, "L-2409-07", joao, hoje.minusDays(30));
        embalagemService.criarEmbalagem(silicone, fb, "Prateleira B2", hoje.plusDays(60), um, "L-2410-02", joao, hoje.minusDays(20));
        embalagemService.criarEmbalagem(silicone, fa, "Prateleira B2", hoje.plusDays(90), um, "L-2411-01", joao, hoje.minusDays(5));
        embalagemService.criarEmbalagem(silicone, fb, "Prateleira B3", hoje.minusDays(10), um, "L-2312-09", joao, hoje.minusDays(300));

        // Itens nao pereciveis: validade longa (a validade e obrigatoria nesta versao)
        embalagemService.criarEmbalagem(parafuso, fa, "Gaveta C1", hoje.plusDays(1000), new BigDecimal("100"), null, joao, hoje.minusDays(10));
        embalagemService.criarEmbalagem(tomada, fb, "Prateleira D1", hoje.plusDays(1500), new BigDecimal("25"), null, joao, hoje.minusDays(10));
        // Vedacao: saldo = minimo (50), entao ja aparece como estoque baixo (RN06: <= minimo)
        embalagemService.criarEmbalagem(epdm, fa, "Rolo - Estante E1", hoje.plusDays(720), new BigDecimal("50"), null, joao, hoje.minusDays(10));
    }

    private Insumo novo(String codigo, String nome, String categoria, String unidade, String minimo) {
        Insumo i = new Insumo();
        i.setCodigo(codigo);
        i.setNome(nome);
        i.setCategoria(categoria);
        i.setUnidadeMedida(unidade);
        i.setEstoqueMinimo(new BigDecimal(minimo).setScale(3));
        return insumoRepository.save(i);
    }
}
