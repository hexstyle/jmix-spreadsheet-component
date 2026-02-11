package com.digtp.scm.view.reference;

import com.digtp.scm.entity.Plant;
import com.digtp.scm.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "plants", layout = MainView.class)
@ViewController("scm_Plant.list")
@ViewDescriptor(path = "/com/digtp/scm/view/reference/plant-list-view.xml")
@LookupComponent("plantsDataGrid")
@DialogMode(width = "48em")
public class PlantListView extends StandardListView<Plant> {
}
