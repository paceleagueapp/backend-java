package com.paceleague.popup.adapter.out.persistence;

import com.paceleague.popup.domain.entity.Popup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PopupJpaRepository extends JpaRepository<Popup, Long> {

    @Query("select p from Popup p where p.active = true and p.startAt <= :now and p.endAt > :now "
            + "order by p.startAt desc, p.sno desc")
    List<Popup> findVisibleAt(@Param("now") LocalDateTime now);
}
