package com.example.scm.repository;

import com.example.scm.entity.Track;
import com.example.scm.entity.User;
import io.jmix.core.repository.JmixDataRepository;
import io.jmix.core.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrackRepository extends JmixDataRepository<Track, UUID> {
    
    @Query("""
            select e
            from scm_Track e
            where e.isActive = true
              and e.isFrozen <> true
              and (e.user = :user or e.user is null)
            order by e.trackType asc""")
    List<Track> findActiveUserTracks(User user);
}