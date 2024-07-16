/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.config;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BldBuildCommand {
    @Nullable
    @NlsSafe
    String getName();

    @Nullable
    @NlsSafe
    String getDisplayName();

    @Nullable
    @Nls(capitalization = Nls.Capitalization.Sentence) String getNotEmptyDescription();

    void run(DataContext dataContext, List<?> additionalProperties, BldBuildListener buildListener);
}
