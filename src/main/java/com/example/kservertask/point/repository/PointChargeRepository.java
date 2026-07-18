package com.example.kservertask.point.repository;

import java.util.Optional;

import com.example.kservertask.point.entity.PointCharge;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointChargeRepository extends JpaRepository<PointCharge, Long> {
    Optional<PointCharge> findByPaymentId(String paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PointCharge p where p.paymentId = :paymentId")
    Optional<PointCharge> findByPaymentIdForUpdate(@Param("paymentId") String paymentId);
}
