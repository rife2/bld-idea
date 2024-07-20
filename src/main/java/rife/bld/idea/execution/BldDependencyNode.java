/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BldDependencyNode {
    private BldDependencyNode parent_ = null;
    private final String name_;
    private final CopyOnWriteArrayList<BldDependencyNode> children_ = new CopyOnWriteArrayList<>();

    BldDependencyNode(String name) {
        name_ = name;
    }

    public String name() {
        return name_;
    }

    public void addChild(BldDependencyNode child) {
        children_.add(child);
        child.parent_ = this;
    }

    public BldDependencyNode parent() {
        return parent_;
    }

    public List<BldDependencyNode> children() {
        return Collections.unmodifiableList(children_);
    }
}
