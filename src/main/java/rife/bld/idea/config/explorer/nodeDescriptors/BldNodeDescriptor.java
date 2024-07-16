/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.config.explorer.nodeDescriptors;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.CellAppearanceEx;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public abstract class BldNodeDescriptor extends NodeDescriptor<Object> implements CellAppearanceEx {
    public BldNodeDescriptor(Project project, NodeDescriptor parentDescriptor) {
        super(project, parentDescriptor);
    }

    @Override
    public void customize(@NotNull SimpleColoredComponent component) {
        component.append(toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }

    @Override
    @NotNull
    @Nls
    public String getText() {
        return toString();
    }
}