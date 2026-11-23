package com.controle.insumos.repository;

import com.esquadralum.insumos.model.Movimentacao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {
    List<Movimentacao> findByEmbalagemIdOrderByDataHoraDescIdDesc(Long embalagemId);
    List<Movimentacao> findAllByOrderByDataHoraDescIdDesc();
    List<Movimentacao> findTop10ByOrderByDataHoraDescIdDesc();
}
