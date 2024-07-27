/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Comparing;
import com.intellij.ui.*;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.config.BldBuildCommand;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.utils.BldBundle;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

public class BldCommandChooserDialog extends DialogWrapper {
    private final Project project_;
    private BldBuildCommand selectedCommand_;
    private Tree tree_;

    public BldCommandChooserDialog(final Project project, final BldBuildCommand selectedTarget) {
        super(project, false);
        project_ = project;
        selectedCommand_ = selectedTarget;

        setTitle(BldBundle.message("bld.command.chooser.title"));
        init();
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        tree_ = initTree();
        tree_.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                    doOKAction();
                }
            }
        });

        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(@NotNull MouseEvent e) {
                if (selectedCommand_ != null) {
                    doOKAction();
                    return true;
                }
                return false;
            }
        }.installOn(tree_);

        return JBUI.Panels.simplePanel(ScrollPaneFactory.createScrollPane(tree_));
    }

    private Tree initTree() {
        @NonNls final var root = new DefaultMutableTreeNode("Root");
        final var tree = new Tree(root);
        tree.getSelectionModel().addTreeSelectionListener(e -> {
            final var selectionPath = tree.getSelectionPath();
            if (selectionPath != null) {
                final var node = (DefaultMutableTreeNode)selectionPath.getLastPathComponent();
                final var userObject = node.getUserObject();
                if (userObject instanceof BldBuildCommand command) {
                    selectedCommand_ = command;
                }
                else {
                    selectedCommand_ = null;
                }
            }
        });
        tree.setCellRenderer(new MyTreeCellRenderer());
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        TreeUtil.installActions(tree);
        TreeSpeedSearch.installOn(tree, false, path -> {
            final var userObject = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
            if (userObject instanceof BldBuildCommand command) {
                return command.displayName();
            }
            return null;
        });

        var selection = processFileTargets(BldConfiguration.instance(project_).getCommands(), root);
        TreeUtil.expandAll(tree);
        TreeUtil.selectInTree(selection, true, tree);
        return tree;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return tree_;
    }

    private DefaultMutableTreeNode processFileTargets(final List<BldBuildCommand> commands, final DefaultMutableTreeNode rootNode) {
        DefaultMutableTreeNode result = null;
        for (var command : commands) {
            final var node = new DefaultMutableTreeNode(command);
            if (isSelected(command)) {
                result = node;
            }
            rootNode.add(node);
        }
        return result;
    }

    private boolean isSelected(final BldBuildCommand command) {
        return selectedCommand_ != null && Comparing.strEqual(selectedCommand_.name(), command.name());
    }

    @Nullable
    public BldBuildCommand getSelectedCommand() {
        return selectedCommand_;
    }

    private static class MyTreeCellRenderer extends ColoredTreeCellRenderer {
        @Override
        public void customizeCellRenderer(@NotNull JTree tree,
                                          Object value,
                                          boolean selected,
                                          boolean expanded,
                                          boolean leaf,
                                          int row,
                                          boolean hasFocus) {
            if (value instanceof DefaultMutableTreeNode treeNode) {
                final Object userObject = treeNode.getUserObject();
                if (userObject instanceof BldBuildCommand command) {
                    append(command.name(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    setIcon(AllIcons.Nodes.Target);
                }
            }
        }
    }
}
