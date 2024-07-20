/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

public class BldExecutionFlags {
    private boolean commands_;
    private boolean dependencyTree_;

    public BldExecutionFlags commands(boolean flag) {
        commands_ = flag;
        return this;
    }

    public BldExecutionFlags dependencyTree(boolean flag) {
        dependencyTree_ = flag;
        return this;
    }

    public boolean commands() {
        return commands_;
    }

    public boolean dependencyTree() {
        return dependencyTree_;
    }
}
