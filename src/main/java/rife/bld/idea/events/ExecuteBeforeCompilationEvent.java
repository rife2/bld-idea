/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.events;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import rife.bld.idea.utils.BldBundle;

public final class ExecuteBeforeCompilationEvent extends ExecutionEvent {
    @NonNls
    public static final String TYPE_ID = "beforeCompilation";

    private static final ExecuteBeforeCompilationEvent INSTANCE = new ExecuteBeforeCompilationEvent();

    private ExecuteBeforeCompilationEvent() {
    }

    public static ExecuteBeforeCompilationEvent instance() {
        return INSTANCE;
    }

    @Override
    public @NonNls String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public @Nls String getPresentableName() {
        return BldBundle.message("bld.event.before.compilation.presentable.name");
    }
}

