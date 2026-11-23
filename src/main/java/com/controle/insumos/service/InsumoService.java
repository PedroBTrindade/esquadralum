package com.controle.insumos.service;

import com.esquadralum.insumos.dto.Dtos.InsumoRequest;
import com.esquadralum.insumos.dto.Dtos.InsumoResponse;
import com.esquadralum.insumos.exception.NaoEncontradoException;
import com.esquadralum.insumos.exception.RegraNegocioException;
import com.esquadralum.insumos.model.Insumo;
import com.esquadralum.insumos.repository.EmbalagemRepository;
import com.esquadralum.insumos.repository.InsumoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InsumoService {
    private final InsumoRepository insumoRepository;
    private final EmbalagemRepository embalagemRepository;
    private final Mapeador mapeador;

    public InsumoService(InsumoRepository insumoRepository, EmbalagemRepository embalagemRepository,
                         Mapeador mapeador) {
        this.insumoRepository = insumoRepository;
        this.embalagemRepository = embalagemRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<InsumoResponse> listar(String busca, String categoria, String situacao, boolean todos) {
        String b = busca == null ? "" : busca.trim().toLowerCase();
        return insumoRepository.findAllByOrderByNomeAsc().stream()
                .filter(i -> todos || i.isAtivo())
                .filter(i -> b.isEmpty() || contem(i.getNome(), b) || contem(i.getCodigo(), b)
                        || contem(i.getCategoria(), b))
                .filter(i -> categoria == null || categoria.isBlank() || categoria.equalsIgnoreCase(i.getCategoria()))
                .map(mapeador::insumo)
                .filter(r -> situacao == null || situacao.isBlank() || situacao.equalsIgnoreCase(r.situacao()))
                .toList();
    }

    private boolean contem(String texto, String trecho) {
        return texto != null && texto.toLowerCase().contains(trecho);
    }

    @Transactional(readOnly = true)
    public InsumoResponse buscar(Long id) {
        return mapeador.insumo(entidade(id));
    }

    @Transactional
    public InsumoResponse criar(InsumoRequest req) {
        String codigo = req.codigo().trim();
        if (insumoRepository.existsByCodigo(codigo)) {
            throw new RegraNegocioException("Já existe um insumo com o código " + codigo + ".");
        }
        Insumo i = new Insumo();
        preencher(i, req);
        return mapeador.insumo(insumoRepository.save(i));
    }

    @Transactional
    public InsumoResponse atualizar(Long id, InsumoRequest req) {
        Insumo i = entidade(id);
        String codigo = req.codigo().trim();
        if (insumoRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw new RegraNegocioException("Já existe outro insumo com o código " + codigo + ".");
        }
        // Trocar a unidade com embalagens ja registradas deixaria os saldos incoerentes.
        if (!i.getUnidadeMedida().equals(req.unidadeMedida().trim()) && embalagemRepository.existsByInsumoId(id)) {
            throw new RegraNegocioException(
                    "Não é possível alterar a unidade de medida: já existem embalagens registradas para este insumo.");
        }
        preencher(i, req);
        return mapeador.insumo(i);
    }

    /** RF04/RN09: desativa em vez de excluir, preservando o historico. */
    @Transactional
    public void desativar(Long id) {
        entidade(id).setAtivo(false);
    }

    private void preencher(Insumo i, InsumoRequest req) {
        i.setCodigo(req.codigo().trim());
        i.setNome(req.nome().trim());
        i.setCategoria(req.categoria() == null ? null : req.categoria().trim());
        i.setDescricao(req.descricao());
        i.setUnidadeMedida(req.unidadeMedida().trim());
        i.setEstoqueMinimo(EstoqueService.normalizar(req.estoqueMinimo()));
    }

    private Insumo entidade(Long id) {
        return insumoRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Insumo não encontrado."));
    }
}
