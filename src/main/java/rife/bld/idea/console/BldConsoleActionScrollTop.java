/*
 * Copyright 2024 Erik C. Thauvin (https://erik.thauvin.net/))
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.console;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.utils.BldBundle;

class BldConsoleActionScrollTop extends DumbAwareAction {
    public BldConsoleActionScrollTop() {
        super(BldBundle.message("bld.action.scroll.top.name"), null,
                AllIcons.RunConfigurations.Scroll_up);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(AnActionEvent event) {
        var project = event.getProject();
        if (project != null) {
            var presentation = event.getPresentation();
            presentation.setEnabled(BldConsoleManager.getConsole(project).getContentSize() != 0);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        var project = e.getProject();
        if (project != null) {
            BldConsoleManager.getConsole(project).scrollTo(0);
        }
    }
}
