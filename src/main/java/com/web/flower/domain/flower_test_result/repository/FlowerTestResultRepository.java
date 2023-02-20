package com.web.flower.domain.flower_test_result.repository;

import com.web.flower.domain.flower_test_result.entity.FlowerTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FlowerTestResultRepository extends JpaRepository<FlowerTestResult, UUID> {

    @Query("SELECT DISTINCT fr from FlowerTestResult fr JOIN FETCH fr.user u WHERE u.username = :username")
    List<FlowerTestResult> findByUsername(@Param("username") String username);

    @Query("SELECT fr from FlowerTestResult fr where fr.user.username = :username and fr.flowerNum = :flowerNum")
    Optional<FlowerTestResult> findByUserAndFlowerNum(@Param("username") String username, @Param("flowerNum") int flowerNum);
}
