package com.example.paceleague.territory.adapter.out.persistence;

import com.example.paceleague.territory.domain.entity.Territory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TerritoryJpaRepository extends JpaRepository<Territory, Long> {

    // bbox 겹침 조건: 두 사각형이 겹치려면 (A.minX <= B.maxX && A.maxX >= B.minX) 가 x/y 모두 성립.
    @Query("""
            select t from Territory t
            where t.status = 'ACTIVE'
              and t.bboxMinLat <= :maxLat and t.bboxMaxLat >= :minLat
              and t.bboxMinLng <= :maxLng and t.bboxMaxLng >= :minLng
            order by t.areaSqm desc
            """)
    List<Territory> findActiveIntersectingBbox(@Param("minLat") BigDecimal minLat,
                                               @Param("minLng") BigDecimal minLng,
                                               @Param("maxLat") BigDecimal maxLat,
                                               @Param("maxLng") BigDecimal maxLng,
                                               Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select t from Territory t
            where t.status = 'ACTIVE'
              and t.bboxMinLat <= :maxLat and t.bboxMaxLat >= :minLat
              and t.bboxMinLng <= :maxLng and t.bboxMaxLng >= :minLng
            order by t.sno asc
            """)
    List<Territory> findActiveIntersectingBboxForUpdate(@Param("minLat") BigDecimal minLat,
                                                        @Param("minLng") BigDecimal minLng,
                                                        @Param("maxLat") BigDecimal maxLat,
                                                        @Param("maxLng") BigDecimal maxLng);
}
