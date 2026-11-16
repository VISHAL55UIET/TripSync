package com.TripSync.memory.repository;

import com.TripSync.memory.entity.MemoryLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemoryLikeRepository extends JpaRepository<MemoryLike, Long> {
    Optional<MemoryLike> findByMemory_IdAndUser_Id(Long memoryId, Long userId);
}