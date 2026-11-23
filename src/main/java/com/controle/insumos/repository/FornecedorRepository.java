package com.controle.insumos.repository;

import com.esquadralum.insumos.model.Fornecedor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    List<Fornecedor> findAllByOrderByNomeAsc();
}
