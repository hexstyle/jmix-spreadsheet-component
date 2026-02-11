package com.digtp.scm.service;

import com.digtp.scm.component.TrackManagementAccess;
import com.digtp.scm.entity.Track;
import com.digtp.scm.entity.TrackType;
import com.digtp.scm.entity.User;
import com.digtp.scm.repository.TrackRepository;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TrackService {
    
    private final TrackRepository trackRepository;
    private final TrackManagementAccess trackManagementAccess;
    private final CurrentUserSubstitution currentUserSubstitution;
    
    public TrackService(TrackRepository trackRepository,
                        TrackManagementAccess trackManagementAccess,
                        CurrentUserSubstitution currentUserSubstitution) {
        this.trackRepository = trackRepository;
        this.trackManagementAccess = trackManagementAccess;
        this.currentUserSubstitution = currentUserSubstitution;
    }
    
    public List<Track> getActiveTracks() {
        User user = (User) currentUserSubstitution.getEffectiveUser();
        return trackRepository.findActiveUserTracks(user).stream()
                .filter(track -> trackManagementAccess.canView(user, track))
                .toList();
    }
    
    public Map<TrackType, Track> getActiveTracksMap() {
        User user = (User) currentUserSubstitution.getEffectiveUser();
        return trackRepository.findActiveUserTracks(user).stream()
                .filter(track -> trackManagementAccess.canView(user, track))
                .collect(Collectors.toMap(
                        Track::getTrackType,
                        Function.identity(),
                        (a, b) -> {
                            throw new IllegalStateException("Cannot exists more that 1 active track for same type %s. Check %s and %s tracks."
                                    .formatted(a.getTrackType().name(), a.getId().toString(), b.getId().toString()));
                        },
                        HashMap::new));
    }
    
    @Transactional
    public Track createPlanningTrack() {
        Track track = trackRepository.create();
        track.setTrackType(TrackType.PLANNING);
        track.setFillingStartDate(LocalDate.now());
        track.setName("Planning");
        track.setIsActive(true);
        return trackRepository.save(track);
    }
    
    @Transactional
    public Track finishTrack(Track track) {
        track.setIsFrozen(true);
        track.setFillingEndDate(LocalDate.now());
        return trackRepository.save(track);
    }
    
    @Transactional
    public Track closeTrack(Track track) {
        track.setIsActive(false);
        return trackRepository.save(track);
    }
}
