package com.paceleague.notification.adapter.out.persistence;

import com.paceleague.notification.domain.entity.NotificationHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationHistoryJpaRepository extends JpaRepository<NotificationHistory, Long> {

    @Query("select n from NotificationHistory n"
            + " where (n.memberSno = :memberSno or (n.memberSno is null and n.createdAt >= :since))"
            + " and (:beforeSno is null or n.sno < :beforeSno)"
            + " order by n.sno desc")
    List<NotificationHistory> findVisibleTo(@Param("memberSno") Long memberSno,
                                            @Param("since") LocalDateTime since,
                                            @Param("beforeSno") Long beforeSno,
                                            Pageable pageable);
}
