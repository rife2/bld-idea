/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.utils.BldBundle;

final class BldProjectActionCommandHelp extends AnAction {
    private final BldProjectWindow projectWindow_;

    public BldProjectActionCommandHelp(BldProjectWindow projectWindow) {
        super(BldBundle.messagePointer("bld.action.command.help.name"),
            BldBundle.messagePointer("bld.action.command.help.description"), AllIcons.Actions.Help);

        projectWindow_ = projectWindow;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        projectWindow_.helpSelection(e.getDataContext());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
