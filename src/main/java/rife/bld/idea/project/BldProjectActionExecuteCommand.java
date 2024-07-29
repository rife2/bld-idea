/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.console.BldConsoleManager;
import rife.bld.idea.execution.BldExecution;
import rife.bld.idea.execution.BldExecutionFlags;
import rife.bld.idea.utils.BldBundle;

import java.util.List;

public final class BldProjectActionExecuteCommand extends DumbAwareAction {
    private final Project project_;
    private final String command_;
    private final String debugString_;

    public BldProjectActionExecuteCommand(final @NotNull Project project,
                                          final String command,
                                          final @NlsActions.ActionDescription String description) {
        project_ = project;

        var template_presentation = getTemplatePresentation();
        template_presentation.setText("Bld Command: " + command, false);
        template_presentation.setDescription(description);
        command_ = command;
        debugString_ = "Command action: " + command +
            "; Project: " + project.getPresentableUrl();
    }

    public @NonNls String toString() {
        return debugString_;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        new Task.Backgroundable(project_, BldBundle.message("bld.project.progress.commands", command_), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                BldConsoleManager.showTaskMessage(BldBundle.message("bld.project.console.commands", command_), ConsoleViewContentType.USER_INPUT, project_);
                BldExecution.instance(project_).executeCommands(new BldExecutionFlags(), List.of(command_));
            }
        }.queue();
    }
}