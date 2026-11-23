package com.controle.insumos.repository;

import com.esquadralum.insumos.model.Insumo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {
    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);
    List<Insumo> findAllByOrderByNomeAsc();
}
