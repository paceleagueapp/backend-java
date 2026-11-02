package com.paceleague.coupon.adapter.out.persistence;

import com.paceleague.coupon.domain.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findBySnoAndMemberSno(Long sno, Long memberSno);

    List<Coupon> findAllByMemberSnoOrderBySnoDesc(Long memberSno);

    boolean existsByImageKey(String imageKey);

    @Modifying
    @Query("delete from Coupon c where c.memberSno = :memberSno")
    void deleteAllByMemberSno(@Param("memberSno") Long memberSno);
}
