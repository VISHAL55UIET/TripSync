package com.TripSync.poll.repository;

import com.TripSync.poll.entity.TripPoll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripPollRepository extends JpaRepository<TripPoll, Long> {
    List<TripPoll> findByTrip_IdOrderByCreatedAtDesc(Long tripId);
}