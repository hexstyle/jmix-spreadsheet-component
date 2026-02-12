package com.example.scm.view.reference;

import com.example.scm.entity.Plant;
import com.example.scm.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "plants", layout = MainView.class)
@ViewController("scm_Plant.list")
@ViewDescriptor(path = "/com/example/scm/view/reference/plant-list-view.xml")
@LookupComponent("plantsDataGrid")
@DialogMode(width = "48em")
public class PlantListView extends StandardListView<Plant> {
}
