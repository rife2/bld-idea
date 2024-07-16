/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.config;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.util.EventDispatcher;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service(Service.Level.PROJECT)
public final class BldConfiguration implements Disposable {
    private final Project project_;
    private final List<BldBuildCommand> buildCommands_ = new CopyOnWriteArrayList<>();
    private final EventDispatcher<BldConfigurationListener> myEventDispatcher = EventDispatcher.create(BldConfigurationListener.class);

    private volatile boolean initialized_ = false;

    public BldConfiguration(final Project project) {
        project_ = project;
        
        ApplicationManager.getApplication().invokeLater(
            () -> myEventDispatcher.getMulticaster().configurationLoaded(),
            ModalityState.any()
        );
    }

    public static BldConfiguration getInstance(final @NotNull Project project) {
        return project.getService(BldConfiguration.class);
    }

    @Override
    public void dispose() {
        // no-op
    }

    public boolean isInitialized() {
        return initialized_;
    }

    public List<BldBuildCommand> getBuildCommandList() {
        return buildCommands_;
    }

    public void setBuildCommandList(ArrayList<BldBuildCommand> commands) {
        initialized_ = true;

        buildCommands_.clear();
        buildCommands_.addAll(commands);
        ApplicationManager.getApplication().invokeLater(
            () -> myEventDispatcher.getMulticaster().configurationChanged(),
            ModalityState.any()
        );
    }

    public void addBldConfigurationListener(final BldConfigurationListener listener) {
        myEventDispatcher.addListener(listener);
    }

    public void removeBldConfigurationListener(final BldConfigurationListener listener) {
        myEventDispatcher.removeListener(listener);
    }
}
