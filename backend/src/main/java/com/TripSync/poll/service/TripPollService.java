package com.TripSync.poll.service;

import com.TripSync.poll.dto.CreatePollRequest;
import com.TripSync.poll.dto.PollResponse;
import com.TripSync.poll.entity.PollOption;
import com.TripSync.poll.entity.PollVote;
import com.TripSync.poll.entity.TripPoll;
import com.TripSync.poll.repository.PollVoteRepository;
import com.TripSync.poll.repository.TripPollRepository;
import com.TripSync.realtime.service.TripRealtimePublisher;
import com.TripSync.trip.entity.Trip;
import com.TripSync.trip.repository.TripRepository;
import com.TripSync.user.entity.User;
import com.TripSync.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripPollService {

    private final TripRealtimePublisher realtimePublisher;
    private final TripPollRepository tripPollRepository;
    private final PollVoteRepository pollVoteRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PollResponse> getTripPolls(Long tripId, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return tripPollRepository.findByTrip_IdOrderByCreatedAtDesc(tripId)
                .stream()
                .map(poll -> toResponse(poll, currentUser.getId()))
                .toList();
    }

    @Transactional
    public PollResponse createPoll(Long tripId, CreatePollRequest req, String userEmail) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TripPoll poll = new TripPoll();
        poll.setTrip(trip);
        poll.setCreatedBy(creator);
        poll.setQuestion(req.question().trim());
        poll.setClosed(false);

        for (String optText : req.options()) {
            if (optText != null && !optText.isBlank()) {
                PollOption option = new PollOption();
                option.setPoll(poll);
                option.setOptionText(optText.trim());
                poll.getOptions().add(option);
            }
        }

        TripPoll saved = tripPollRepository.save(poll);
        realtimePublisher.publish(
                tripId,
                "POLL",
                "CREATED",
                creator.getFullName() + " created a new poll: \"" + saved.getQuestion() + "\"",
                userEmail
        );
        return toResponse(saved, creator.getId());
    }

    @Transactional
    public PollResponse castVote(Long tripId, Long pollId, Long optionId, String userEmail) {
        TripPoll poll = tripPollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));

        if (!poll.getTrip().getId().equals(tripId)) {
            throw new RuntimeException("Poll does not belong to this trip");
        }
        if (poll.isClosed()) {
            throw new RuntimeException("This poll is closed for voting");
        }

        User voter = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PollOption selectedOption = poll.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Option not found"));

        PollVote vote = pollVoteRepository.findByPoll_IdAndUser_Id(pollId, voter.getId())
                .orElseGet(() -> {
                    PollVote v = new PollVote();
                    v.setPoll(poll);
                    v.setUser(voter);
                    return v;
                });

        if (vote.getOption() != null && !vote.getOption().getId().equals(optionId)) {
            vote.getOption().getVotes().remove(vote);
        }

        vote.setOption(selectedOption);
        if (!selectedOption.getVotes().contains(vote)) {
            selectedOption.getVotes().add(vote);
        }

        pollVoteRepository.save(vote);
        realtimePublisher.publish(
                tripId,
                "POLL",
                "VOTED",
                voter.getFullName() + " voted on \"" + poll.getQuestion() + "\"",
                userEmail
        );
        return toResponse(poll, voter.getId());
    }

    @Transactional
    public PollResponse toggleClosePoll(Long tripId, Long pollId, String userEmail) {
        TripPoll poll = tripPollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        poll.setClosed(!poll.isClosed());
        TripPoll saved = tripPollRepository.save(poll);
        realtimePublisher.publish(
                tripId,
                "POLL",
                "UPDATED",
                user.getFullName() + (saved.isClosed() ? " locked poll: \"" : " reopened poll: \"") + saved.getQuestion() + "\"",
                userEmail
        );
        return toResponse(saved, user.getId());
    }

    @Transactional
    public void deletePoll(Long tripId, Long pollId) {
        TripPoll poll = tripPollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        String question = poll.getQuestion();
        tripPollRepository.delete(poll);
        realtimePublisher.publish(
                tripId,
                "POLL",
                "DELETED",
                "Poll \"" + question + "\" was deleted",
                null
        );
    }

    private PollResponse toResponse(TripPoll poll, Long currentUserId) {
        int totalVotes = poll.getOptions().stream()
                .mapToInt(o -> o.getVotes().size())
                .sum();

        Long userVotedOptionId = null;
        for (PollOption opt : poll.getOptions()) {
            boolean votedThis = opt.getVotes().stream()
                    .anyMatch(v -> v.getUser().getId().equals(currentUserId));
            if (votedThis) {
                userVotedOptionId = opt.getId();
                break;
            }
        }

        List<PollResponse.OptionSummary> optionSummaries = poll.getOptions().stream()
                .map(opt -> {
                    int count = opt.getVotes().size();
                    double pct = totalVotes > 0 ? Math.round((count * 1000.0) / totalVotes) / 10.0 : 0.0;
                    List<String> voters = opt.getVotes().stream()
                            .map(v -> v.getUser().getFullName())
                            .toList();
                    return new PollResponse.OptionSummary(
                            opt.getId(),
                            opt.getOptionText(),
                            count,
                            pct,
                            voters
                    );
                })
                .toList();

        return new PollResponse(
                poll.getId(),
                poll.getQuestion(),
                poll.isClosed(),
                poll.getCreatedBy().getFullName(),
                poll.getCreatedBy().getId(),
                poll.getCreatedAt(),
                totalVotes,
                userVotedOptionId,
                optionSummaries
        );
    }
}