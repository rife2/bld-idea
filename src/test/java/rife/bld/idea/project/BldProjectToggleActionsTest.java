/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.execution.BldExecution;

public class BldProjectToggleActionsTest extends BasePlatformTestCase {
    public void testShowConsoleIconFollowsTheSetting() {
        var configuration = BldConfiguration.instance(getProject());
        try {
            var action = new BldProjectActionShowConsole(getProject());
            configuration.setActivateConsoleOnExecute(true);
            assertTrue(updatedIconIsSelected(action));
            configuration.setActivateConsoleOnExecute(false);
            assertFalse(updatedIconIsSelected(action));
        } finally {
            configuration.setActivateConsoleOnExecute(false);
        }
    }

    public void testOfflineIconFollowsTheSetting() {
        var execution = BldExecution.instance(getProject());
        try {
            var action = new BldProjectActionOffline(getProject());
            execution.setOffline(true);
            assertTrue(updatedIconIsSelected(action));
            execution.setOffline(false);
            assertFalse(updatedIconIsSelected(action));
        } finally {
            execution.setOffline(false);
        }
    }

    // the toolbar calls update to decide how a toggle is drawn
    private boolean updatedIconIsSelected(ToggleAction action) {
        var event = AnActionEvent.createFromDataContext("test", new Presentation(), SimpleDataContext.getProjectContext(getProject()));
        action.update(event);
        return Toggleable.isSelected(event.getPresentation());
    }
}
