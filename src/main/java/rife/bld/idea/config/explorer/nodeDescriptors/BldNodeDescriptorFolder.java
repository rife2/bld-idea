/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.config.explorer.nodeDescriptors;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.CellAppearanceEx;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.openapi.util.Comparing;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public final class BldNodeDescriptorFolder extends BldNodeDescriptor {
    private final Object folder_;
    private final String name_;
    private CompositeAppearance highlightedText_;

    public BldNodeDescriptorFolder(final Project project, final NodeDescriptor parentDescriptor, final Object folder, final String name) {
        super(project, parentDescriptor);
        folder_ = folder;
        name_ = name;
        highlightedText_ = new CompositeAppearance();
    }

    @Override
    public Object getElement() {
        return folder_;
    }

    @Override
    public boolean update() {
        final var oldText = highlightedText_;
        setIcon(AllIcons.Nodes.Folder);

        highlightedText_ = new CompositeAppearance();

        final var color = UIUtil.getLabelForeground();
        var nameAttributes = new TextAttributes(color, null, null, EffectType.BOXED, Font.PLAIN);
        highlightedText_.getEnding().addText(name_, nameAttributes);

        myName = highlightedText_.getText();

        return !Comparing.equal(highlightedText_, oldText);
    }

    public CellAppearanceEx getHighlightedText() {
        return highlightedText_;
    }

    @Override
    public void customize(@NotNull SimpleColoredComponent component) {
        getHighlightedText().customize(component);
        component.setIcon(getIcon());
        component.setToolTipText(null);
    }
}