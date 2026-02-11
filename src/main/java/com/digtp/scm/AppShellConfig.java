package com.digtp.scm;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

@Push
@Theme(value = "jmix-spreadsheet")
@PWA(name = "Jmix Spreadsheet", shortName = "Jmix Spreadsheet", offline = false)
public class AppShellConfig implements AppShellConfigurator {
}
