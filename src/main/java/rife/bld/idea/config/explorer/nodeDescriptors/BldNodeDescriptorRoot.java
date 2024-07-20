/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.config.explorer.nodeDescriptors;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;

public final class BldNodeDescriptorRoot extends BldNodeDescriptor {
    private final Object root_;

    public BldNodeDescriptorRoot(Project project, NodeDescriptor parentDescriptor, Object root) {
        super(project, parentDescriptor);
        root_ = root;
    }

    @Override
    public Object getElement() {
        return root_;
    }

    @Override
    public boolean update() {
        myName = "";
        return false;
    }
}
