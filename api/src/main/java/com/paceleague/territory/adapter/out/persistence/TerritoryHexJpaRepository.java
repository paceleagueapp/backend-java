package com.paceleague.territory.adapter.out.persistence;

import com.paceleague.territory.domain.entity.TerritoryHex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TerritoryHexJpaRepository extends JpaRepository<TerritoryHex, Long> {

    @Query(value = """
            select th.territory_sno as territorySno,
                   count(*)         as overlapHexCount
            from territory_hex th
            join territory t on t.sno = th.territory_sno
            where t.status = 'ACTIVE' and th.h3_index in (:hexIndexes)
            group by th.territory_sno
            """, nativeQuery = true)
    List<TerritoryHexOverlapProjection> findActiveOverlapCounts(@Param("hexIndexes") List<Long> hexIndexes);

    List<TerritoryHex> findByTerritorySnoIn(List<Long> territorySnos);
}
