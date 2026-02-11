package com.digtp.scm.portbalance.columns;

import com.digtp.scm.entity.Track;
import com.digtp.scm.entity.TrackType;

public record TrackKey(String name, TrackType trackType) {

    public static TrackKey from(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Track is required");
        }
        return new TrackKey(track.getName(), track.getTrackType());
    }
}
