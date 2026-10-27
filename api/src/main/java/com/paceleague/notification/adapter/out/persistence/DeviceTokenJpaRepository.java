package com.paceleague.notification.adapter.out.persistence;

import com.paceleague.notification.domain.entity.DeviceToken;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DeviceTokenJpaRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByFcmId(String fcmId);

    @Query("select d from DeviceToken d where (:afterSno is null or d.sno > :afterSno) order by d.sno asc")
    List<DeviceToken> findPageAfter(@Param("afterSno") Long afterSno, Pageable pageable);

    @Modifying
    @Query("delete from DeviceToken d where d.fcmId = :fcmId and d.memberSno = :memberSno")
    void deleteByFcmIdAndMemberSno(@Param("fcmId") String fcmId, @Param("memberSno") Long memberSno);

    @Modifying
    @Query("delete from DeviceToken d where d.fcmId in :fcmIds")
    void deleteAllByFcmIdIn(@Param("fcmIds") Collection<String> fcmIds);

    @Modifying
    @Query("delete from DeviceToken d where d.memberSno = :memberSno")
    void deleteAllByMemberSno(@Param("memberSno") Long memberSno);
}
