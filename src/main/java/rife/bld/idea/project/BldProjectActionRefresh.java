/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.console.BldConsoleManager;
import rife.bld.idea.execution.BldRefresh;
import rife.bld.idea.utils.BldBundle;

final class BldProjectActionRefresh extends AnAction implements DumbAware {
    private final Project project_;

    public BldProjectActionRefresh(Project project) {
        super(BldBundle.messagePointer("bld.action.refresh.name"),
            BldBundle.messagePointer("bld.action.refresh.description"), AllIcons.Actions.Refresh);
        project_ = project;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        FileDocumentManager.getInstance().saveAllDocuments();
        BldConsoleManager.showTaskMessage(BldBundle.message("bld.project.console.refresh"), ConsoleViewContentType.USER_INPUT, project_);
        BldRefresh.instance(project_).refresh();
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        final var presentation = event.getPresentation();
        presentation.setText(BldBundle.messagePointer("bld.action.refresh.name"));
        presentation.setEnabled(true);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
