/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.execution.BldExecution;
import rife.bld.idea.utils.BldBundle;

import java.io.IOException;

final class BldProjectActionClearCache extends AnAction implements DumbAware {
    private final Project project_;

    public BldProjectActionClearCache(Project project) {
        super(BldBundle.messagePointer("bld.action.clearCache.name"),
            BldBundle.messagePointer("bld.action.clearCache.description"), AllIcons.Actions.GC);

        project_ = project;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var cache = BldExecution.getInstance(project_).getBldCache();
        if (cache != null) {
            ApplicationManager.getApplication().runWriteAction(
                () -> {
                    try {
                        cache.delete(null);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            );
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        final var presentation = event.getPresentation();
        presentation.setText(BldBundle.messagePointer("bld.action.clearCache.name"));
        presentation.setEnabled(BldExecution.getInstance(project_).hasBldCache());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
