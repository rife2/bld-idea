/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.RunManager;
import com.intellij.execution.impl.RunDialog;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.execution.BldRunConfiguration;
import rife.bld.idea.execution.BldRunConfigurationType;
import rife.bld.idea.utils.BldBundle;

final class BldProjectActionMakeRunConfiguration extends AnAction implements DumbAware {
    private final Project project_;
    private final BldProjectWindow projectWindow_;

    BldProjectActionMakeRunConfiguration(Project project, BldProjectWindow projectWindow) {
        super(BldBundle.messagePointer("bld.make.run.configuration.name"), AllIcons.General.Settings);

        project_ = project;
        projectWindow_ = projectWindow;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Presentation presentation = e.getPresentation();
        presentation.setEnabled(projectWindow_.hasSingleSelection() && projectWindow_.canRunSelection());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var command = projectWindow_.getSelectedBuildCommand();
        if (command == null) {
            return;
        }

        var run_manager = RunManager.getInstance(project_);
        var settings = run_manager.createConfiguration(command.name(), BldRunConfigurationType.class);
        var configuration = (BldRunConfiguration) settings.getConfiguration();
        configuration.acceptSettings(command);
        if (RunDialog.editConfiguration(e.getProject(), settings, ExecutionBundle
            .message("create.run.configuration.for.item.dialog.title", configuration.getName()))) {
            run_manager.addConfiguration(settings);
            run_manager.setSelectedConfiguration(settings);
        }
    }
}
