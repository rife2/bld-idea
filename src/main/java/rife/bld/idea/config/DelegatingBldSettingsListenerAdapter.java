/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.config;

import com.intellij.openapi.externalSystem.settings.DelegatingExternalSystemSettingsListener;
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.settings.BldProjectSettings;
import rife.bld.idea.settings.BldSettingsListener;

public class DelegatingBldSettingsListenerAdapter extends DelegatingExternalSystemSettingsListener<BldProjectSettings>
    implements BldSettingsListener
{

    public DelegatingBldSettingsListenerAdapter(@NotNull ExternalSystemSettingsListener<BldProjectSettings> delegate) {
        super(delegate);
    }
}
