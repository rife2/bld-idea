/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.console;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.errorTreeView.NewErrorTreeRenderer;
import com.intellij.ide.errorTreeView.impl.ErrorTreeViewConfiguration;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AutoScrollToSourceHandler;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SideBorder;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.execution.BldExecution;
import rife.bld.idea.utils.BldBundle;

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

        add(createToolbarPanel(), BorderLayout.WEST);

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

    private static class StopAction extends DumbAwareAction {
        public StopAction() {
            super(IdeBundle.message("action.stop"), null, AllIcons.Actions.Suspend);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            var project = e.getProject();
            if (project != null) {
                BldExecution.terminateBldProcess(project);
                BldConsoleManager.getConsole(project).print(BldBundle.message("bld.command.terminated"), ConsoleViewContentType.ERROR_OUTPUT);
            }
        }

        @Override
        public void update(AnActionEvent event) {
            // Make the action only clickable when there is an active bld process
            var presentation = event.getPresentation();
            var project = event.getProject();
            if (project == null) {
                return;
            }
            presentation.setEnabled(BldExecution.hasActiveBldProcess(project));
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
        }
    }

    private JPanel createToolbarPanel() {
        var action = new StopAction();

        var group = new DefaultActionGroup();
        group.add(action);

        var toolbar_panel = new JPanel(new BorderLayout());
//        var action_manager = ActionManager.getInstance();
//        var left_toolbar = action_manager.createActionToolbar(ActionPlaces.COMPILER_MESSAGES_TOOLBAR, group, false);
//        toolbar_panel.add(left_toolbar.getComponent(), BorderLayout.WEST);

        return toolbar_panel;
    }
}
