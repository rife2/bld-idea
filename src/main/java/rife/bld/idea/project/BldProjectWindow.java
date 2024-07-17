/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.execution.RunManagerListener;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
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
import rife.bld.idea.config.explorer.nodeDescriptors.BldCommandNodeDescriptor;
import rife.bld.idea.console.BldConsoleManager;
import rife.bld.idea.execution.BldExecution;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.config.explorer.BldExplorerTreeStructure;
import rife.bld.idea.config.explorer.nodeDescriptors.BldNodeDescriptor;
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

        new EditSourceOnDoubleClickHandler.TreeMouseListener(tree_, null) {
            @Override
            protected void processDoubleClick(@NotNull MouseEvent e, @NotNull DataContext dataContext, @NotNull TreePath treePath) {
                runSelection(DataManager.getInstance().getDataContext(tree_), true);
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

        treeModel_ = null;

        project_ = null;
        config_ = null;
    }

    private JPanel createToolbarPanel() {
        final var group = new DefaultActionGroup();
        group.add(new RefreshAction());
        group.add(new RunAction());
        group.addSeparator();
        group.add(new EditAction());

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

    private void runSelection(final DataContext dataContext, final boolean moveFocusToEditor) {
        if (!canRunSelection()) {
            return;
        }

        final var commands = getCommandNamesFromPaths(tree_.getSelectionPaths());

        var commands_info = String.join(" ", commands);
        new Task.Backgroundable(project_, BldBundle.message("bld.project.progress.commands", commands_info), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                BldConsoleManager.showTaskMessage(BldBundle.message("bld.project.console.commands", commands_info), ConsoleViewContentType.USER_INPUT, project_);
                BldExecution.getInstance(project_).executeCommands(false, commands);
            }
        }.queue();

        if (moveFocusToEditor) {
            ToolWindowManager.getInstance(project_).activateEditorComponent();
        }
    }

    private boolean canRunSelection() {
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
            return userObject instanceof  BldCommandNodeDescriptor;
        }
        return true;
    }

    private static List<@NlsSafe String> getCommandNamesFromPaths(TreePath[] paths) {
        if (paths == null || paths.length == 0) {
            return Collections.emptyList();
        }
        final List<String> targets = new ArrayList<>();
        for (final TreePath path : paths) {
            final Object userObject = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
            if (!(userObject instanceof BldCommandNodeDescriptor)) {
                continue;
            }
            final BldBuildCommand target = ((BldCommandNodeDescriptor)userObject).getCommand();
            targets.add(target.name());
        }
        return targets;
    }

    private final class RefreshAction extends AnAction implements DumbAware {
        public RefreshAction() {
            super(BldBundle.messagePointer("refresh.bld.command.action.name"),
                BldBundle.messagePointer("refresh.bld.command.action.description"), AllIcons.Actions.Refresh);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            new Task.Backgroundable(project_, BldBundle.message("bld.project.progress.refresh"), true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    BldConsoleManager.showTaskMessage(BldBundle.message("bld.project.console.refresh"), ConsoleViewContentType.USER_INPUT, project_);

                    BldExecution.getInstance(project_).listTasks();
                }
            }.queue();
        }

        @Override
        public void update(@NotNull AnActionEvent event) {
            final var presentation = event.getPresentation();
            presentation.setText(BldBundle.messagePointer("refresh.bld.command.action.name"));
            presentation.setEnabled(true);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }
    }

    private final class RunAction extends AnAction implements DumbAware {
        public RunAction() {
            super(BldBundle.messagePointer("run.bld.command.action.name"),
                BldBundle.messagePointer("run.bld.command.action.description"), AllIcons.Actions.Execute);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            runSelection(e.getDataContext(), true);
        }

        @Override
        public void update(@NotNull AnActionEvent event) {
            final var presentation = event.getPresentation();
            presentation.setText(BldBundle.messagePointer("run.bld.command.action.name"));
            presentation.setEnabled(true);
            presentation.setEnabled(canRunSelection());
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }
    }

    private final class EditAction extends AnAction implements DumbAware {
        public EditAction() {
            super(BldBundle.messagePointer("edit.bld.command.action.name"),
                BldBundle.messagePointer("edit.bld.command.action.description"), AllIcons.Actions.EditSource);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            var main_class = BldExecution.getInstance(project_).getBldMainClass();
            var psi_class =JavaPsiFacade.getInstance(project_).findClass(main_class, GlobalSearchScope.allScope(project_));
            if (psi_class != null) {
                FileEditorManager.getInstance(project_).openFile(psi_class.getContainingFile().getVirtualFile());
            }
        }

        @Override
        public void update(@NotNull AnActionEvent event) {
            final var presentation = event.getPresentation();
            presentation.setText(BldBundle.messagePointer("edit.bld.command.action.name"));
            presentation.setEnabled(true);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }
    }

}
