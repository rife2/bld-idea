/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.execution.RunManagerListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TreeUIHelper;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.config.BldConfigurationListener;
import rife.bld.idea.project.actions.RefreshAction;
import rife.bld.idea.project.actions.RunAction;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.config.explorer.BldExplorerTreeStructure;
import rife.bld.idea.config.explorer.nodeDescriptors.BldNodeDescriptor;
import rife.bld.idea.utils.BldBundle;
import rife.bld.idea.utils.BldConstants;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public final class BldProjectWindow extends SimpleToolWindowPanel implements DataProvider, Disposable {
    private Project project_;
    private Tree tree_;
    private BldConfiguration config_;
    private StructureTreeModel<?> treeModel_;

    public BldProjectWindow(@NotNull Project project) {
        super(true, true);

        project_ = project;
        config_ = BldConfiguration.getInstance(project);

        setupTree(project);

        setToolbar(createToolbarPanel());
        setContent(ScrollPaneFactory.createScrollPane(tree_));
        ToolTipManager.sharedInstance().registerComponent(tree_);

        setupEmptyText();
    }

    private void setupTree(@NotNull Project project) {
        var tree_structure = new BldExplorerTreeStructure(project);
        final var treeModel = new StructureTreeModel<>(tree_structure, this);
        treeModel_ = treeModel;
        tree_ = new Tree(new AsyncTreeModel(treeModel, this));
        tree_.setRootVisible(false);
        tree_.setShowsRootHandles(true);
        tree_.setCellRenderer(new NodeRenderer());

        final var listener = new BldConfigurationListener() {
            @Override
            public void configurationLoaded() {
                treeModel.invalidateAsync();
            }
            @Override
            public void configurationChanged() {
                treeModel.invalidateAsync();
            }
        };
        config_.addBldConfigurationListener(listener);
        Disposer.register(this, () -> config_.removeBldConfigurationListener(listener));

        TreeUtil.installActions(tree_);
        TreeUIHelper.getInstance().installTreeSpeedSearch(tree_);

        project.getMessageBus().connect(this).subscribe(RunManagerListener.TOPIC, new RunManagerListener() {
            @Override
            public void beforeRunTasksChanged () {
                treeModel.invalidateAsync();
            }
        });
    }

    private void setupEmptyText() {
        var emptyText = tree_.getEmptyText();
        emptyText.appendLine(BldBundle.message("bld.empty.text"));
    }

    @Override
    public void dispose() {
        final var tree = tree_;
        if (tree != null) {
            ToolTipManager.sharedInstance().unregisterComponent(tree);
            for (var keyStroke : tree.getRegisteredKeyStrokes()) {
                tree.unregisterKeyboardAction(keyStroke);
            }
            tree_ = null;
        }

        treeModel_ = null;

        project_ = null;
        config_ = null;
    }

    private JPanel createToolbarPanel() {
        final var group = new DefaultActionGroup();
        group.add(new RefreshAction());
        group.add(new RunAction());

        final var action_toolbar = ActionManager.getInstance().createActionToolbar(BldConstants.BLD_EXPLORER_TOOLBAR, group, true);
        action_toolbar.setTargetComponent(this);
        return JBUI.Panels.simplePanel(action_toolbar.getComponent());
    }

    private static final class NodeRenderer extends ColoredTreeCellRenderer {
        @Override
        public void customizeCellRenderer(@NotNull JTree tree,
                                          Object value,
                                          boolean selected,
                                          boolean expanded,
                                          boolean leaf,
                                          int row,
                                          boolean hasFocus) {
            final var user_object = ((DefaultMutableTreeNode)value).getUserObject();
            if (user_object instanceof BldNodeDescriptor descriptor) {
                descriptor.customize(this);
            }
            else {
                append(tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        }
    }

}
