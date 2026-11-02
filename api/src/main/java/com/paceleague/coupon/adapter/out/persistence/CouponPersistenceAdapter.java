package com.paceleague.coupon.adapter.out.persistence;

import com.paceleague.coupon.application.port.out.CouponRepositoryPort;
import com.paceleague.coupon.domain.entity.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CouponPersistenceAdapter implements CouponRepositoryPort {

    private final CouponJpaRepository jpaRepository;

    @Override
    public Coupon save(Coupon coupon) {
        return jpaRepository.save(coupon);
    }

    @Override
    public Optional<Coupon> findBySno(Long sno) {
        return jpaRepository.findById(sno);
    }

    @Override
    public Optional<Coupon> findBySnoAndMemberSno(Long sno, Long memberSno) {
        return jpaRepository.findBySnoAndMemberSno(sno, memberSno);
    }

    @Override
    public List<Coupon> findAllByMemberSno(Long memberSno) {
        return jpaRepository.findAllByMemberSnoOrderBySnoDesc(memberSno);
    }

    @Override
    public boolean existsByImageKey(String imageKey) {
        return jpaRepository.existsByImageKey(imageKey);
    }

    @Override
    public void delete(Coupon coupon) {
        jpaRepository.delete(coupon);
    }

    @Override
    public void deleteAllByMemberSno(Long memberSno) {
        jpaRepository.deleteAllByMemberSno(memberSno);
    }
}
