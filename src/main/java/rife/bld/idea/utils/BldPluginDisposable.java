/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.utils;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * The service is intended to be used instead of a project/application as a parent disposable.
 */
@Service({Service.Level.APP, Service.Level.PROJECT})
public final class BldPluginDisposable implements Disposable {
    public static @NotNull Disposable instance() {
        return ApplicationManager.getApplication().getService(BldPluginDisposable.class);
    }

    public static @NotNull Disposable instance(@NotNull Project project) {
        return project.getService(BldPluginDisposable.class);
    }

    @Override
    public void dispose() {
    }
}