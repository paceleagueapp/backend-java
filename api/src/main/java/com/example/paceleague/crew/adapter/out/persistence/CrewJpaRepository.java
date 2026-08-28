package com.example.paceleague.crew.adapter.out.persistence;

import com.example.paceleague.crew.domain.entity.Crew;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CrewJpaRepository extends JpaRepository<Crew, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Crew c where c.sno = :sno")
    Optional<Crew> findBySnoForUpdate(@Param("sno") Long sno);

    boolean existsByNameAndStatus(String name, String status);

    @Query("""
            select c from Crew c
            where c.status = 'ACTIVE'
              and (:q = '' or lower(c.name) like lower(concat('%', :q, '%')))
            order by c.name asc
            """)
    List<Crew> searchActive(@Param("q") String q, Pageable pageable);
}
