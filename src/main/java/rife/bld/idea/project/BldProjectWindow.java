/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.execution.RunManagerListener;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TreeUIHelper;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.config.BldBuildCommand;
import rife.bld.idea.config.BldConfigurationListener;
import rife.bld.idea.config.explorer.nodeDescriptors.BldNodeDescriptorCommand;
import rife.bld.idea.console.BldConsoleManager;
import rife.bld.idea.execution.BldExecution;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.config.explorer.BldExplorerTreeStructure;
import rife.bld.idea.config.explorer.nodeDescriptors.BldNodeDescriptor;
import rife.bld.idea.execution.BldExecutionFlags;
import rife.bld.idea.utils.BldBundle;
import rife.bld.idea.utils.BldConstants;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BldProjectWindow extends SimpleToolWindowPanel implements DataProvider, Disposable {
    private Project project_;
    private Tree tree_;
    private BldConfiguration config_;

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

        new EditSourceOnDoubleClickHandler.TreeMouseListener(tree_, null) {
            @Override
            protected void processDoubleClick(@NotNull MouseEvent e, @NotNull DataContext dataContext, @NotNull TreePath treePath) {
                runSelection(DataManager.getInstance().getDataContext(tree_));
            }
        }.installOn(tree_);

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

        project_ = null;
        config_ = null;
    }

    private JPanel createToolbarPanel() {
        final var group = new DefaultActionGroup();
        group.add(new BldProjectActionRefresh(project_));
        group.add(new BldProjectActionRun(this));
        group.addSeparator();
        group.add(new BldProjectActionEditMain(project_));
        group.add(new BldProjectActionEditProperties(project_));
        group.addSeparator();
        group.add(new BldProjectActionClearCache(project_));
        group.addSeparator();
        group.add(new BldProjectActionOffline(project_));

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

    void runSelection(DataContext dataContext) {
        if (!canRunSelection()) {
            return;
        }

        // save all documents
        FileDocumentManager.getInstance().saveAllDocuments();

        // execute the selected commands
        final var commands = getCommandNamesFromPaths(tree_.getSelectionPaths());
        var commands_info = String.join(" ", commands);
        new Task.Backgroundable(project_, BldBundle.message("bld.project.progress.commands", commands_info), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                BldConsoleManager.showTaskMessage(BldBundle.message("bld.project.console.commands", commands_info), ConsoleViewContentType.USER_INPUT, project_);
                BldExecution.getInstance(project_).executeCommands(new BldExecutionFlags(), commands);
            }
        }.queue();

        // move focus to editor
        ToolWindowManager.getInstance(project_).activateEditorComponent();
    }

    boolean canRunSelection() {
        if (tree_ == null) {
            return false;
        }
        final TreePath[] paths = tree_.getSelectionPaths();
        if (paths == null) {
            return false;
        }
        for (final TreePath path : paths) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            final Object userObject = node.getUserObject();
            return userObject instanceof BldNodeDescriptorCommand;
        }
        return true;
    }

    private static List<String> getCommandNamesFromPaths(TreePath[] paths) {
        if (paths == null || paths.length == 0) {
            return Collections.emptyList();
        }
        final List<String> targets = new ArrayList<>();
        for (final TreePath path : paths) {
            final Object userObject = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
            if (!(userObject instanceof BldNodeDescriptorCommand)) {
                continue;
            }
            final BldBuildCommand target = ((BldNodeDescriptorCommand)userObject).getCommand();
            targets.add(target.name());
        }
        return targets;
    }

}
