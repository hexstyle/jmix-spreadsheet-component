package com.example.scm.component;
import com.example.scm.entity.Track;
import com.example.scm.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TrackManagementAccess {
    public boolean canView(User user, Track entity) {
        return true;
    }
}
