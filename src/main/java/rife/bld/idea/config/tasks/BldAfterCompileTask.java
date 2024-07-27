/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.config.tasks;

import com.intellij.openapi.compiler.CompileContext;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.config.BldConfiguration;

final class BldAfterCompileTask extends BldCompileTask {
    @Override
    public boolean execute(@NotNull CompileContext context) {
        return BldConfiguration.instance(context.getProject()).executeCommandAfterCompile(context, createDataContext(context));
    }
}
