package com.example.scm.portbalance.columns;

import com.example.scm.entity.Track;
import com.example.scm.entity.TrackType;

public record TrackKey(String name, TrackType trackType) {

    public static TrackKey from(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Track is required");
        }
        return new TrackKey(track.getName(), track.getTrackType());
    }
}
