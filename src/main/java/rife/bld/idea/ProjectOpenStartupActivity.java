/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.execution.BldExecution;

public class ProjectOpenStartupActivity implements ProjectActivity {
    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        System.out.println("Startup");

        BldExecution.listTasks(project);

        /* Clear console window. */
//        ApplicationManager.getApplication().invokeAndWait(() -> {
//            ConsoleView executionConsole = BldConsoleManager.getConsole(project);
//            executionConsole.getComponent().setVisible(true);
//            executionConsole.clear();
//            var console = ToolWindowManagerEx.getInstance(project).getToolWindow(BldConstants.BLD_CONSOLE_NAME);
//            if (console != null) {
//                console.activate(null);
//            }
//        }, ModalityState.nonModal());

        return null;
    }
}
