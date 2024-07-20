/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.utils.BldBundle;

final class BldProjectActionRun extends AnAction implements DumbAware {
    private final BldProjectWindow projectWindow_;

    public BldProjectActionRun(BldProjectWindow projectWindow) {
        super(BldBundle.messagePointer("bld.action.run.name"),
            BldBundle.messagePointer("bld.action.run.description"), AllIcons.Actions.Execute);

        projectWindow_ = projectWindow;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        projectWindow_.runSelection(e.getDataContext());
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        final var presentation = event.getPresentation();
        presentation.setText(BldBundle.messagePointer("bld.action.run.name"));
        presentation.setEnabled(true);
        presentation.setEnabled(projectWindow_.canRunSelection());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
