/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.settings;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings;
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.XCollection;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.config.DelegatingBldSettingsListenerAdapter;

import java.util.Set;
import java.util.TreeSet;

@Service(Service.Level.PROJECT)
@State(name = "BldSettings", storages = @Storage("bld.xml"))
public final class BldSettings extends AbstractExternalSystemSettings<BldSettings, BldProjectSettings, BldSettingsListener>
    implements PersistentStateComponent<BldSettings.MyState> {

    public BldSettings(@NotNull Project project) {
        super(BldSettingsListener.TOPIC, project);
    }

    @NotNull
    public static BldSettings getInstance(@NotNull Project project) {
        return project.getService(BldSettings.class);
    }

    @Override
    public void subscribe(@NotNull ExternalSystemSettingsListener<BldProjectSettings> listener, @NotNull Disposable parentDisposable) {
        doSubscribe(new DelegatingBldSettingsListenerAdapter(listener), parentDisposable);
    }

    @Override
    protected void copyExtraSettingsFrom(@NotNull BldSettings settings) {
    }

    @Override
    public BldSettings.@NotNull MyState getState() {
        MyState state = new MyState();
        fillState(state);

        return state;
    }

    @Override
    public void loadState(@NotNull MyState state) {
        super.loadState(state);

        if (ApplicationManager.getApplication().isUnitTestMode()) {
            return;
        }
    }

    @Override
    protected void checkSettings(@NotNull BldProjectSettings old, @NotNull BldProjectSettings current) {
    }

    public static class MyState implements State<BldProjectSettings> {

        private final Set<BldProjectSettings> myProjectSettings = new TreeSet<>();

        @Override
        @XCollection(elementTypes = BldProjectSettings.class)
        public Set<BldProjectSettings> getLinkedExternalProjectsSettings() {
            return myProjectSettings;
        }

        @Override
        public void setLinkedExternalProjectsSettings(Set<BldProjectSettings> settings) {
            if (settings != null) {
                myProjectSettings.addAll(settings);
            }
        }
    }
}