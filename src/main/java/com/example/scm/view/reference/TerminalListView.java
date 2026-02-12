package com.example.scm.view.reference;

import com.example.scm.entity.Terminal;
import com.example.scm.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "terminals", layout = MainView.class)
@ViewController("scm_Terminal.list")
@ViewDescriptor(path = "/com/example/scm/view/reference/terminal-list-view.xml")
@LookupComponent("terminalsDataGrid")
@DialogMode(width = "48em")
public class TerminalListView extends StandardListView<Terminal> {
}
