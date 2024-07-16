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

public final class RunAction extends AnAction implements DumbAware {
    public RunAction() {
        super(BldBundle.messagePointer("run.bld.command.action.name"),
            BldBundle.messagePointer("run.bld.command.action.description"), AllIcons.Actions.Execute);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
//            runSelection(e.getDataContext(), true);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        final var presentation = event.getPresentation();
        presentation.setText(BldBundle.messagePointer("run.bld.command.action.name"));
        presentation.setEnabled(true);
//            presentation.setEnabled(canRunSelection());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
