/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import icons.BldIcons;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.utils.BldBundle;

public final class BldRunConfigurationType extends SimpleConfigurationType {
    public BldRunConfigurationType() {
        super("AntRunConfiguration", BldBundle.message("bld.configuration.type.name.command"),
            BldBundle.message("bld.configuration.type.description.run.bld.command"), NotNullLazyValue.lazy(() -> BldIcons.Action));
    }

    @Override
    public String getHelpTopic() {
        return "reference.dialogs.rundebug.AntRunConfiguration";
    }

    @NotNull
    public static BldRunConfigurationType instance() {
        return ConfigurationTypeUtil.findConfigurationType(BldRunConfigurationType.class);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new BldRunConfiguration(project, this);
    }
}
