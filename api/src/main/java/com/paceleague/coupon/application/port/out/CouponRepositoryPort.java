package com.paceleague.coupon.application.port.out;

import com.paceleague.coupon.domain.entity.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponRepositoryPort {

    Coupon save(Coupon coupon);

    Optional<Coupon> findBySno(Long sno);

    Optional<Coupon> findBySnoAndMemberSno(Long sno, Long memberSno);

    // 최근 지급순.
    List<Coupon> findAllByMemberSno(Long memberSno);

    boolean existsByImageKey(String imageKey);

    void delete(Coupon coupon);

    void deleteAllByMemberSno(Long memberSno);
}
