/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.console;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.errorTreeView.NewErrorTreeRenderer;
import com.intellij.ide.errorTreeView.impl.ErrorTreeViewConfiguration;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AutoScrollToSourceHandler;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SideBorder;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.utils.BldConstants;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class BldConsoleViewPanel extends JPanel {
    protected static final Logger LOG = Logger.getInstance("#com.intellij.ide.errorTreeView.NewErrorTreeViewPanel");

    private final ErrorTreeViewConfiguration configuration_;
    protected Project project_;

    public BldConsoleViewPanel(Project project, ConsoleView console) {
        project_ = project;
        configuration_ = ErrorTreeViewConfiguration.getInstance(project);
        setLayout(new BorderLayout());

        AutoScrollToSourceHandler auto_scroll_to_source = new AutoScrollToSourceHandler() {
            @Override
            protected boolean isAutoScrollMode() {
                return configuration_.isAutoscrollToSource();
            }

            @Override
            protected void setAutoScrollMode(boolean state) {
                configuration_.setAutoscrollToSource(state);
            }
        };

        var message_panel = new JPanel(new BorderLayout());

        var root = new DefaultMutableTreeNode();
        final var tree_model = new DefaultTreeModel(root);
        var tree = createTree(tree_model);
        tree.getEmptyText().setText(IdeBundle.message("errortree.noMessages"));

        auto_scroll_to_source.install(tree);
        TreeUtil.installActions(tree);

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setLargeModel(true);

        var scroll_pane = NewErrorTreeRenderer.install(tree);
        scroll_pane.setBorder(IdeBorderFactory.createBorder(SideBorder.LEFT));
        message_panel.add(console.getComponent(), BorderLayout.CENTER);

        add(createToolbarPanel(), BorderLayout.EAST);

        add(message_panel, BorderLayout.CENTER);

        EditSourceOnDoubleClickHandler.install(tree);
    }

    @NotNull
    protected Tree createTree(@NotNull final DefaultTreeModel treeModel) {
        return new Tree(treeModel) {
            @Override
            public void setRowHeight(int i) {
                super.setRowHeight(0);
                // this is needed in order to make UI calculate the height for each particular row
            }
        };
    }

    private JPanel createToolbarPanel() {
        var group = new DefaultActionGroup();
        group.add(new BldConsoleActionStop());
        group.add(new BldConsoleActionClear());

        var toolbar_panel = new JPanel(new BorderLayout());
        var action_manager = ActionManager.getInstance();
        var left_toolbar = action_manager.createActionToolbar(BldConstants.BLD_CONSOLE_TOOLBAR, group, false);
        left_toolbar.setTargetComponent(this);
        toolbar_panel.add(left_toolbar.getComponent(), BorderLayout.EAST);

        return toolbar_panel;
    }
}
