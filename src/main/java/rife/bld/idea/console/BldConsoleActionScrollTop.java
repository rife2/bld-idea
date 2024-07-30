/*
 * Copyright 2024 Erik C. Thauvin (https://erik.thauvin.net/))
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.console;

import com.intellij.icons.AllIcons;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

class BldConsoleActionScrollTop extends DumbAwareAction {
    public BldConsoleActionScrollTop() {
        super(ActionsBundle.message("action.EditorScrollTop.text"), null,
                AllIcons.RunConfigurations.Scroll_up);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        var project = e.getProject();
        if (project != null) {
            BldConsoleManager.getConsole(project).scrollTo(0);
        }
    }
}
