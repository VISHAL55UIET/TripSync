package com.TripSync.poll.repository;

import com.TripSync.poll.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    Optional<PollVote> findByPoll_IdAndUser_Id(Long pollId, Long userId);
}