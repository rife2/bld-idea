/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BldDependencyTree extends CopyOnWriteArrayList<BldDependencyNode> {
    public final BldDependencyNode extensions = new BldDependencyNode("Extensions");
    public final BldDependencyNode compile = new BldDependencyNode("Compile");
    public final BldDependencyNode provided = new BldDependencyNode("Provided");
    public final BldDependencyNode runtime = new BldDependencyNode("Runtime");
    public final BldDependencyNode test = new BldDependencyNode("Test");

    public BldDependencyTree() {
        addAll(List.of(extensions, compile, provided, runtime, test));
    }
}
