package com.digtp.scm.component;
import com.digtp.scm.entity.Track;
import com.digtp.scm.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TrackManagementAccess {
    public boolean canView(User user, Track entity) {
        return true;
    }
}
