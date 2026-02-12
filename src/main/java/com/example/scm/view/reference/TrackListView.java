package com.example.scm.view.reference;

import com.example.scm.entity.Track;
import com.example.scm.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "tracks", layout = MainView.class)
@ViewController("scm_Track.list")
@ViewDescriptor(path = "/com/example/scm/view/reference/track-list-view.xml")
@LookupComponent("tracksDataGrid")
@DialogMode(width = "48em")
public class TrackListView extends StandardListView<Track> {
}
