/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.config.explorer.nodeDescriptors;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.Nls;

public final class BldNodeDescriptorText extends BldNodeDescriptor {
    public BldNodeDescriptorText(Project project, NodeDescriptor parentDescriptor, @Nls String text) {
        super(project, parentDescriptor);
        myName = text;
        myColor = JBColor.blue;
    }

    @Override
    public Object getElement() {
        return myName;
    }

    @Override
    public boolean update() {
        return true;
    }
}
