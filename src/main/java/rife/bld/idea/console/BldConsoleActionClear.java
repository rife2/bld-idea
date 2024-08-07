/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.console;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

class BldConsoleActionClear extends DumbAwareAction {
    public BldConsoleActionClear() {
        super(IdeBundle.message("terminal.action.ClearBuffer.text"), null, AllIcons.Actions.GC);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        var project = e.getProject();
        if (project != null) {
            BldConsoleManager.getConsole(project).clear();
        }
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
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
