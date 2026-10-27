package com.paceleague.popup.application.port.out;

import com.paceleague.popup.domain.entity.Popup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PopupRepositoryPort {
    Popup save(Popup popup);

    Optional<Popup> findById(Long sno);

    Page<Popup> findAll(Pageable pageable);

    // active = true and startAt <= now < endAt, 시작일 최신순
    List<Popup> findVisibleAt(LocalDateTime now);

    void delete(Popup popup);
}
