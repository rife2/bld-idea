/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.utils.BldBundle;

public final class RefreshAction extends AnAction implements DumbAware {
    public RefreshAction() {
        super(BldBundle.messagePointer("refresh.bld.command.action.name"),
            BldBundle.messagePointer("refresh.bld.command.action.description"), AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        final var presentation = event.getPresentation();
        presentation.setText(BldBundle.messagePointer("refresh.bld.command.action.name"));
        presentation.setEnabled(true);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
