/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.keymap.impl.ui.EditKeymapsDialog;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.utils.BldBundle;

final class BldProjectActionAssignShortcut extends AnAction {
    private final Project project_;
    private final String actionId_;

    BldProjectActionAssignShortcut(Project project, String actionId) {
        super(BldBundle.message("bld.project.assign.shortcut.action.name"));
        project_ = project;
        actionId_ = actionId;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new EditKeymapsDialog(project_, actionId_).show();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(actionId_ != null && ActionManager.getInstance().getAction(actionId_) != null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
