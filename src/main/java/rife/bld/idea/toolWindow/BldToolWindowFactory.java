/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.toolWindow;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class BldToolWindowFactory implements ToolWindowFactory, DumbAware {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        var window = new BldToolWindow(toolWindow);
        var content = ContentFactory.getInstance().createContent(window.getContentPanel(), null, false);
        toolWindow.getContentManager().addContent(content);
    }

    private static class BldToolWindow {
        private final JBPanel<?> contentPanel_;

        public BldToolWindow(ToolWindow toolWindow) {
            contentPanel_ = new JBPanel<>(new BorderLayout());
            contentPanel_.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            contentPanel_.add(createControlsPanel(toolWindow), BorderLayout.NORTH);
            contentPanel_.add(new JBLabel("This is the bld tool window"), BorderLayout.CENTER);
        }

        public JBPanel<?> getContentPanel() {
            return contentPanel_;
        }

        @NotNull
        private JPanel createControlsPanel(ToolWindow toolWindow) {
            var controlsPanel = new JPanel();
            var refreshDateAndTimeButton = new JButton("Refresh");
            refreshDateAndTimeButton.addActionListener(e -> {});
            controlsPanel.add(refreshDateAndTimeButton);
            var hideToolWindowButton = new JButton("Hide");
            hideToolWindowButton.addActionListener(e -> toolWindow.hide(null));
            controlsPanel.add(hideToolWindowButton);
            return controlsPanel;
        }
    }
}
