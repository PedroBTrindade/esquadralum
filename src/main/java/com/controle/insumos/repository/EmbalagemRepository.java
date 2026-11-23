package com.controle.insumos.repository;

import com.esquadralum.insumos.model.Embalagem;
import com.esquadralum.insumos.model.Insumo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmbalagemRepository extends JpaRepository<Embalagem, Long> {
    Optional<Embalagem> findByCodigo(String codigo);
    boolean existsByInsumoId(Long insumoId);
    List<Embalagem> findByInsumoIdOrderByValidadeAscIdAsc(Long insumoId);
    List<Embalagem> findAllByOrderByValidadeAscIdAsc();

    /** Soma dos saldos utilizaveis: DISPONIVEL e nao vencida. Pode retornar null. */
    @Query("select sum(e.saldo) from Embalagem e where e.insumo.id = :insumoId "
         + "and e.situacao = com.esquadralum.insumos.model.SituacaoEmbalagem.DISPONIVEL "
         + "and e.validade >= :hoje")
    BigDecimal somaDisponivel(@Param("insumoId") Long insumoId, @Param("hoje") LocalDate hoje);

    @Query("select distinct e.insumo from Embalagem e where e.fornecedor.id = :fornecedorId")
    List<Insumo> insumosDoFornecedor(@Param("fornecedorId") Long fornecedorId);
}
