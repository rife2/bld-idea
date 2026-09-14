/*
 * Copyright 2026 Erik C. Thauvin (https://erik.thauvin.net/)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.utils.BldBundle;

final class BldProjectActionShowConsole extends ToggleAction implements DumbAware {
    private final Project project_;

    public BldProjectActionShowConsole(Project project) {
        super(BldBundle.messagePointer("bld.action.showConsole.name"),
            BldBundle.messagePointer("bld.action.showConsole.description"), AllIcons.Debugger.Console);
        project_ = project;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return BldConfiguration.instance(project_).isActivateConsoleOnExecute();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        BldConfiguration.instance(project_).setActivateConsoleOnExecute(state);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
