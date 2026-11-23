package com.controle.insumos.service;

import com.esquadralum.insumos.dto.Dtos.FornecedorRequest;
import com.esquadralum.insumos.dto.Dtos.FornecedorResponse;
import com.esquadralum.insumos.exception.NaoEncontradoException;
import com.esquadralum.insumos.model.Fornecedor;
import com.esquadralum.insumos.repository.EmbalagemRepository;
import com.esquadralum.insumos.repository.FornecedorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FornecedorService {
    private final FornecedorRepository fornecedorRepository;
    private final EmbalagemRepository embalagemRepository;
    private final Mapeador mapeador;

    public FornecedorService(FornecedorRepository fornecedorRepository, EmbalagemRepository embalagemRepository,
                             Mapeador mapeador) {
        this.fornecedorRepository = fornecedorRepository;
        this.embalagemRepository = embalagemRepository;
        this.mapeador = mapeador;
    }

    @Transactional(readOnly = true)
    public List<FornecedorResponse> listar() {
        return fornecedorRepository.findAllByOrderByNomeAsc().stream()
                .map(f -> mapeador.fornecedor(f, null)).toList();
    }

    /** Detalhe inclui os insumos ja fornecidos. As embalagens: GET /embalagens?fornecedorId= */
    @Transactional(readOnly = true)
    public FornecedorResponse buscar(Long id) {
        Fornecedor f = entidade(id);
        return mapeador.fornecedor(f, embalagemRepository.insumosDoFornecedor(id));
    }

    @Transactional
    public FornecedorResponse criar(FornecedorRequest req) {
        Fornecedor f = new Fornecedor(req.nome().trim(), req.contato());
        return mapeador.fornecedor(fornecedorRepository.save(f), null);
    }

    @Transactional
    public FornecedorResponse atualizar(Long id, FornecedorRequest req) {
        Fornecedor f = entidade(id);
        f.setNome(req.nome().trim());
        f.setContato(req.contato());
        return mapeador.fornecedor(f, null);
    }

    private Fornecedor entidade(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Fornecedor não encontrado."));
    }
}
