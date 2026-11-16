package com.TripSync.memory.repository;

import com.TripSync.memory.entity.TripMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripMemoryRepository extends JpaRepository<TripMemory, Long> {
    List<TripMemory> findByTrip_IdOrderByCreatedAtDesc(Long tripId);
}