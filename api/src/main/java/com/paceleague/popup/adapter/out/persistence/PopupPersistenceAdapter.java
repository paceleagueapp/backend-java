package com.paceleague.popup.adapter.out.persistence;

import com.paceleague.popup.application.port.out.PopupRepositoryPort;
import com.paceleague.popup.domain.entity.Popup;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PopupPersistenceAdapter implements PopupRepositoryPort {

    private final PopupJpaRepository jpaRepository;

    @Override
    public Popup save(Popup popup) {
        return jpaRepository.save(popup);
    }

    @Override
    public Optional<Popup> findById(Long sno) {
        return jpaRepository.findById(sno);
    }

    @Override
    public Page<Popup> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public List<Popup> findVisibleAt(LocalDateTime now) {
        return jpaRepository.findVisibleAt(now);
    }

    @Override
    public void delete(Popup popup) {
        jpaRepository.delete(popup);
    }
}
