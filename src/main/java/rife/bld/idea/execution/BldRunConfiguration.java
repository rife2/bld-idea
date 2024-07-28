/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.config.BldBuildCommand;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.utils.BldBundle;

import java.util.Collections;
import java.util.List;

public final class BldRunConfiguration extends LocatableConfigurationBase<BldRunConfiguration> implements RunProfileWithCompileBeforeLaunchOption {
    BldRunConfigurationSettings settings_ = new BldRunConfigurationSettings();

    public BldRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory) {
        super(project, factory);
    }

    @Override
    public RunConfiguration clone() {
        final BldRunConfiguration configuration = (BldRunConfiguration) super.clone();
        configuration.settings_ = settings_.copy();
        return configuration;
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new BldRunConfigurationSettingsEditor(this);
    }

    @Override
    public void checkConfiguration()
    throws RuntimeConfigurationException {
        if (!BldConfiguration.instance(getProject()).isInitialized()) {
            throw new RuntimeConfigurationException(BldBundle.message("bld.dialog.message.config.not.initialized"));
        }
        if (getCommand() == null)
            throw new RuntimeConfigurationException(BldBundle.message("bld.dialog.message.command.not.specified"),
                BldBundle.message("bld.dialog.title.config.missing.parameters"));
    }

    @Override
    public String suggestedName() {
        var command = getCommand();
        return command == null ? null : command.displayName();
    }

    @NotNull
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) {
        return new BldRunProfileState(env);
    }

    @Override
    public void readExternal(@NotNull Element element)
    throws InvalidDataException {
        super.readExternal(element);
        settings_.readExternal(element);
    }

    @Override
    public void writeExternal(@NotNull Element element)
    throws WriteExternalException {
        super.writeExternal(element);
        settings_.writeExternal(element);
    }

    @Nullable
    public BldBuildCommand getCommand() {
        return BldConfiguration.instance(getProject()).findCommand(settings_.commandName_);
    }

    @NotNull
    public List<BldRunProperty> getProperties() {
        return Collections.unmodifiableList(settings_.properties_);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean acceptSettings(BldBuildCommand command) {
        settings_.commandName_ = command.name();
        return true;
    }

    static void copyProperties(final Iterable<BldRunProperty> from, final List<? super BldRunProperty> to) {
        to.clear();
        for (BldRunProperty p : from) {
            to.add(p.clone());
        }
    }
}
