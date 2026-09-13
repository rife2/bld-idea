/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.console;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import rife.bld.idea.utils.BldConstants;

import javax.swing.*;
import java.awt.*;

public class BldConsoleViewPanel extends JPanel {
    protected Project project_;

    public BldConsoleViewPanel(Project project, ConsoleView console) {
        project_ = project;
        setLayout(new BorderLayout());

        var message_panel = new JPanel(new BorderLayout());
        message_panel.add(console.getComponent(), BorderLayout.CENTER);

        add(createToolbarPanel(), BorderLayout.EAST);

        add(message_panel, BorderLayout.CENTER);
    }

    private JPanel createToolbarPanel() {
        var group = new DefaultActionGroup();
        group.add(new BldConsoleActionStop());
        group.add(new BldConsoleActionScrollTop());
        group.add(new BldConsoleActionScrollEnd());
        group.add(new BldConsoleActionClear());

        var toolbar_panel = new JPanel(new BorderLayout());
        var action_manager = ActionManager.getInstance();
        var left_toolbar = action_manager.createActionToolbar(BldConstants.BLD_CONSOLE_TOOLBAR, group, false);
        left_toolbar.setTargetComponent(this);
        toolbar_panel.add(left_toolbar.getComponent(), BorderLayout.EAST);

        return toolbar_panel;
    }
}
