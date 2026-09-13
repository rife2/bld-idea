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
import rife.bld.idea.execution.BldRefresh;

public class ProjectOpenStartupActivity implements ProjectActivity {
    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        var execution = BldExecution.instance(project);
        if (!execution.setupProject()) {
            return null;
        }

        var refresh = BldRefresh.instance(project);
        refresh.watch(execution.getProjectDir());
        refresh.refresh();
        return null;
    }
}
