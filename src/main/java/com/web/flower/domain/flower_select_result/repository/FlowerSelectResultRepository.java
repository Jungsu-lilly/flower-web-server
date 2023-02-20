package com.web.flower.domain.flower_select_result.repository;

import com.web.flower.domain.flower_select_result.entity.FlowerSelectResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FlowerSelectResultRepository extends JpaRepository<FlowerSelectResult, UUID> {

    @Query("SELECT DISTINCT fs from FlowerSelectResult fs JOIN FETCH fs.user u WHERE u.username = :username")
    List<FlowerSelectResult> findByUsername(@Param("username") String username);

    @Query("SELECT fs from FlowerSelectResult fs where fs.user.username = :username and fs.flowerNum = :flowerNum")
    Optional<FlowerSelectResult> findByUserAndFlowerNum(@Param("username") String username, @Param("flowerNum") int flowerNum);
}
