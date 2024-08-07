/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.config.explorer.nodeDescriptors;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.CellAppearanceEx;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.openapi.util.Comparing;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.config.BldBuildCommand;
import rife.bld.idea.config.BldConfiguration;

import java.awt.*;
import java.util.ArrayList;

public final class BldNodeDescriptorCommand extends BldNodeDescriptor {
    private static final TextAttributes POSTFIX_ATTRIBUTES = new TextAttributes(JBColor.gray, null, null, EffectType.BOXED, Font.PLAIN);

    private final BldBuildCommand command_;
    private CompositeAppearance highlightedText_;

    public BldNodeDescriptorCommand(final Project project, final NodeDescriptor parentDescriptor, final BldBuildCommand command) {
        super(project, parentDescriptor);
        command_ = command;
        highlightedText_ = new CompositeAppearance();
    }

    @Override
    public Object getElement() {
        return command_;
    }

    public BldBuildCommand getCommand() {
        return command_;
    }

    @Override
    public boolean update() {
        final var oldText = highlightedText_;
        setIcon(AllIcons.Nodes.Target);

        highlightedText_ = new CompositeAppearance();

        final var color = UIUtil.getLabelForeground();
        var nameAttributes = new TextAttributes(color, null, null, EffectType.BOXED, Font.PLAIN);
        highlightedText_.getEnding().addText(command_.name(), nameAttributes);

        myName = highlightedText_.getText();

        addShortcutText(getCommand().actionId());

        var configuration = BldConfiguration.instance(myProject);
        final var added_names = new ArrayList<String>(4);
        for (final var event : configuration.getEventsForCommand(command_)) {
            final String presentable_name = event.getPresentableName();
            if (!added_names.contains(presentable_name)) {
                added_names.add(presentable_name);
                highlightedText_.getEnding().addText(" (" + presentable_name + ')', POSTFIX_ATTRIBUTES);
            }
        }

        return !Comparing.equal(highlightedText_, oldText);
    }

    private void addShortcutText(String actionId) {
        var shortcut = KeymapUtil.getPrimaryShortcut(actionId);
        if (shortcut != null) {
            highlightedText_.getEnding().addText(" (" + KeymapUtil.getShortcutText(shortcut) + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
    }

    public CellAppearanceEx getHighlightedText() {
        return highlightedText_;
    }

    @Override
    public void customize(@NotNull SimpleColoredComponent component) {
        getHighlightedText().customize(component);
        component.setIcon(getIcon());
        var toolTipText = getCommand().description();
        component.setToolTipText(toolTipText);
    }
}