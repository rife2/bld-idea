/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.execution.RunManagerListener;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.DataManager;
import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.*;
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
import rife.bld.idea.events.ExecuteAfterCompilationEvent;
import rife.bld.idea.events.ExecuteBeforeCompilationEvent;
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
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class BldProjectWindow extends SimpleToolWindowPanel implements DataProvider, Disposable {
    private Project project_;
    private StructureTreeModel treeModel_;
    private Tree tree_;
    private BldConfiguration config_;

    public BldProjectWindow(@NotNull Project project) {
        super(true, true);

        project_ = project;
        config_ = BldConfiguration.instance(project);

        setupTree(project);

        setToolbar(createToolbarPanel());
        setContent(ScrollPaneFactory.createScrollPane(tree_));
        ToolTipManager.sharedInstance().registerComponent(tree_);

        setupEmptyText();
    }

    private void setupTree(@NotNull Project project) {
        var tree_structure = new BldExplorerTreeStructure(project);
        treeModel_ = new StructureTreeModel<>(tree_structure, this);
        tree_ = new Tree(new AsyncTreeModel(treeModel_, this));
        tree_.setRootVisible(false);
        tree_.setShowsRootHandles(true);
        tree_.setCellRenderer(new NodeRenderer());

        final var listener = new BldConfigurationListener() {
            @Override
            public void configurationLoaded() {
                treeModel_.invalidateAsync();
            }
            @Override
            public void configurationChanged() {
                treeModel_.invalidateAsync();
            }
        };
        config_.addBldConfigurationListener(listener);
        Disposer.register(this, () -> config_.removeBldConfigurationListener(listener));

        TreeUtil.installActions(tree_);
        TreeUIHelper.getInstance().installTreeSpeedSearch(tree_);

        tree_.addMouseListener(new PopupHandler() {
            @Override
            public void invokePopup(final Component comp, final int x, final int y) {
                popupInvoked(comp, x, y);
            }
        });

        new EditSourceOnDoubleClickHandler.TreeMouseListener(tree_, null) {
            @Override
            protected void processDoubleClick(@NotNull MouseEvent e, @NotNull DataContext dataContext, @NotNull TreePath treePath) {
                runSelection(DataManager.getInstance().getDataContext(tree_));
            }
        }.installOn(tree_);

        project.getMessageBus().connect(this).subscribe(RunManagerListener.TOPIC, new RunManagerListener() {
            @Override
            public void beforeRunTasksChanged () {
                treeModel_.invalidateAsync();
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

    private final TreeExpander treeExpander_ = new DefaultTreeExpander(() -> tree_) {
        @Override
        protected boolean isEnabled(@NotNull JTree tree) {
            final BldConfiguration config = config_;
            return config != null && super.isEnabled(tree);
        }
    };

    private JPanel createToolbarPanel() {
        final var group = new DefaultActionGroup();
        group.add(new BldProjectActionRefresh(project_));
        group.add(new BldProjectActionRun(this));
        group.addSeparator();
        group.add(new BldProjectActionEditMain(project_));
        group.add(new BldProjectActionEditProperties(project_));
        group.addSeparator();
        AnAction action = CommonActionsManager.getInstance().createExpandAllAction(treeExpander_, this);
        action.getTemplatePresentation().setDescription(BldBundle.messagePointer("bld.action.expand.all.description"));
        group.add(action);
        action = CommonActionsManager.getInstance().createCollapseAllAction(treeExpander_, this);
        action.getTemplatePresentation().setDescription(BldBundle.messagePointer("bld.action.collapse.all.description"));
        group.add(action);
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

    private void executeBuildCommands(List<String> commands) {
        var commands_info = String.join(" ", commands);
        new Task.Backgroundable(project_, BldBundle.message("bld.project.progress.commands", commands_info), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                BldConsoleManager.showTaskMessage(BldBundle.message("bld.project.console.commands", commands_info), ConsoleViewContentType.USER_INPUT, project_);
                BldExecution.instance(project_).executeCommands(new BldExecutionFlags(), commands);
            }
        }.queue();
    }

    boolean hasSingleSelection() {
        return tree_.getSelectionCount() == 1;
    }

    void runSelection(DataContext dataContext) {
        if (!canRunSelection()) {
            return;
        }

        // save all documents
        FileDocumentManager.getInstance().saveAllDocuments();

        // execute the selected commands
        executeBuildCommands(getCommandNamesFromPaths(tree_.getSelectionPaths()));

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

    BldBuildCommand getSelectedBuildCommand() {
        var selection_path = tree_.getSelectionPath();
        if (selection_path == null) return null;

        final var node = (DefaultMutableTreeNode) selection_path.getLastPathComponent();
        final var user_object = node.getUserObject();
        BldBuildCommand command = null;
        if (user_object instanceof BldNodeDescriptorCommand node_descriptor) {
            command = node_descriptor.getCommand();
        }
        return command;
    }

    void helpSelection(DataContext dataContext) {
        var command = getSelectedBuildCommand();
        if (command == null) {
            return;
        }

        // execute the selected commands
        executeBuildCommands(List.of("help", command.name()));

        // move focus to editor
        Objects.requireNonNull(ToolWindowManager.getInstance(project_).getToolWindow(BldConstants.CONSOLE_NAME)).activate(null);
    }

    private static List<String> getCommandNamesFromPaths(TreePath[] paths) {
        if (paths == null || paths.length == 0) {
            return Collections.emptyList();
        }
        final var commands = new ArrayList<String>();
        for (final var path : paths) {
            final var userObject = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
            if (!(userObject instanceof BldNodeDescriptorCommand)) {
                continue;
            }
            final var command = ((BldNodeDescriptorCommand)userObject).getCommand();
            commands.add(command.name());
        }
        return commands;
    }

    private void popupInvoked(final Component comp, final int x, final int y) {
        Object userObject = null;
        final var path = tree_.getSelectionPath();
        if (path != null) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node != null) {
                userObject = node.getUserObject();
            }
        }

        if (!(userObject instanceof BldNodeDescriptorCommand command_node)) {
            return;
        }

        final var group = new DefaultActionGroup();
        group.add(new BldProjectActionRun(this));
        group.add(new BldProjectActionCommandHelp(this));
        group.add(new BldProjectActionMakeRunConfiguration(project_, this));
        final var command = command_node.getCommand();
        final var execute_on_group = DefaultActionGroup.createPopupGroup(BldBundle.messagePointer("bld.project.execute.on.action.group.name"));
        execute_on_group.add(new BldProjectActionExecuteOnEvent(project_, treeModel_, command, ExecuteBeforeCompilationEvent.instance()));
        execute_on_group.add(new BldProjectActionExecuteOnEvent(project_, treeModel_, command, ExecuteAfterCompilationEvent.instance()));
        group.add(execute_on_group);

        final var popup_menu = ActionManager.getInstance().createActionPopupMenu(BldConstants.BLD_EXPLORER_POPUP, group);
        popup_menu.getComponent().show(comp, x, y);
    }

}
