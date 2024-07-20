/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.console;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.execution.BldExecution;
import rife.bld.idea.utils.BldBundle;

class BldConsoleStopAction extends DumbAwareAction {
    public BldConsoleStopAction() {
        super(IdeBundle.message("action.stop"), null, AllIcons.Actions.Suspend);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        var project = e.getProject();
        if (project != null) {
            BldExecution.getInstance(project).terminateBldProcess();
            BldConsoleManager.getConsole(project).print(BldBundle.message("bld.command.terminated"), ConsoleViewContentType.ERROR_OUTPUT);
        }
    }

    @Override
    public void update(AnActionEvent event) {
        // Make the action only clickable when there is an active bld process
        var presentation = event.getPresentation();
        var project = event.getProject();
        if (project == null) {
            return;
        }
        presentation.setEnabled(BldExecution.getInstance(project).hasActiveBldProcess());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
