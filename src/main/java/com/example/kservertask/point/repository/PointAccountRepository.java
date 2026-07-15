package com.example.kservertask.point.repository;

import com.example.kservertask.point.entity.PointAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointAccountRepository extends JpaRepository<PointAccount, Long> {
}
