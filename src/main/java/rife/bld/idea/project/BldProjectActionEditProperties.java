/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.execution.BldExecution;
import rife.bld.idea.utils.BldBundle;

final class BldProjectActionEditProperties extends AnAction implements DumbAware {
    private final Project project_;

    public BldProjectActionEditProperties(Project project) {
        super(BldBundle.messagePointer("bld.action.properties.name"),
            BldBundle.messagePointer("bld.action.properties.description"), AllIcons.Actions.EditScheme);

        project_ = project;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var properties = BldExecution.getInstance(project_).getBldProperties();
        if (properties != null) {
            FileEditorManager.getInstance(project_).openFile(properties);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        final var presentation = event.getPresentation();
        presentation.setText(BldBundle.messagePointer("bld.action.properties.name"));
        presentation.setEnabled(BldExecution.getInstance(project_).hasBldProperties());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
