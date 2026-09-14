/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.execution.BldExecution;
import rife.bld.idea.utils.BldBundle;

final class BldProjectActionShowConsole extends ToggleAction implements DumbAware {
    private final Project project_;

    public BldProjectActionShowConsole(Project project) {
        super(BldBundle.messagePointer("bld.action.showConsole.name"),
            BldBundle.messagePointer("bld.action.showConsole.description"), AllIcons.Debugger.Console);
        project_ = project;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        boolean state = !isSelected(e);
        setSelected(e, state);
        Presentation presentation = e.getPresentation();
        Toggleable.setSelected(presentation, state);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return BldExecution.instance(project_).isActivateConsoleOnExecute();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        BldExecution.instance(project_).setActivateConsoleOnExecute(state);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        final var presentation = event.getPresentation();
        presentation.setText(BldBundle.messagePointer("bld.action.showConsole.name"));
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
