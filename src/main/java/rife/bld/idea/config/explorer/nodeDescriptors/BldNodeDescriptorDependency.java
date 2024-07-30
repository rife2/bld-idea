/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.config.explorer.nodeDescriptors;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleColoredComponent;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.execution.BldDependencyNode;

public final class BldNodeDescriptorDependency extends BldNodeDescriptor {
    private final BldDependencyNode node_;

    public BldNodeDescriptorDependency(Project project, NodeDescriptor parentDescriptor, BldDependencyNode node) {
        super(project, parentDescriptor);
        node_ = node;
        myName = node.name();
    }

    public BldDependencyNode getNode() {
        return node_;
    }

    @Override
    public Object getElement() {
        return node_;
    }

    @Override
    public boolean update() {
        if (node_.parent() == null) {
            setIcon(AllIcons.Nodes.PpLibFolder);
        }
        else {
            setIcon(AllIcons.Nodes.PpLib);
        }
        return true;
    }

    @Override
    public void customize(@NotNull SimpleColoredComponent component) {
        component.setIcon(getIcon());
        component.append(node_.name());
        component.setToolTipText(null);
    }

}
