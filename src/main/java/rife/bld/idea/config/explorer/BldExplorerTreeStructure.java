/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.config.explorer;

import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.config.BldBuildCommand;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.config.explorer.nodeDescriptors.*;
import rife.bld.idea.execution.BldDependencyNode;
import rife.bld.idea.utils.BldBundle;

import java.util.ArrayList;
import java.util.Comparator;

public final class BldExplorerTreeStructure extends AbstractTreeStructure {
    private static final Logger LOG = Logger.getInstance(BldExplorerTreeStructure.class);

    private final Project project_;

    private final Object root_ = new Object();
    private final Object commandsFolder_ = new Object();
    private final Object dependenciesFolder_ = new Object();

    public BldExplorerTreeStructure(final Project project) {
        project_ = project;
    }

    @Override
    public boolean isToBuildChildrenInBackground(@NotNull final Object element) {
        return true;
    }

    @Override
    public boolean isAlwaysLeaf(@NotNull Object element) {
        return element != root_ &&
            element != commandsFolder_ &&
            element != dependenciesFolder_ &&
            !(element instanceof BldDependencyNode);
    }

    @Override
    @NotNull
    public BldNodeDescriptor createDescriptor(@NotNull Object element, NodeDescriptor parentDescriptor) {
        if (element == root_) {
            return new BldNodeDescriptorRoot(project_, parentDescriptor, root_);
        }
        if (element == commandsFolder_) {
            return new BldNodeDescriptorFolder(project_, parentDescriptor, commandsFolder_, BldBundle.message("bld.project.commands"));
        }
        if (element == dependenciesFolder_) {
            return new BldNodeDescriptorFolder(project_, parentDescriptor, dependenciesFolder_, BldBundle.message("bld.project.dependencies"));
        }

        if (element instanceof BldBuildCommand command) {
            return new BldNodeDescriptorCommand(project_, parentDescriptor, command);
        }

        if (element instanceof BldDependencyNode node) {
            return new BldNodeDescriptorDependency(project_, parentDescriptor, node);
        }

        if (element instanceof String) {
            return new BldNodeDescriptorText(project_, parentDescriptor, (String) element);
        }

        LOG.error("Unknown element for this tree structure " + element);
        return new BldNodeDescriptorText(project_, parentDescriptor, String.valueOf(element));
    }

    @Override
    public Object[] getChildElements(@NotNull Object element) {
        final var configuration = BldConfiguration.instance(project_);
        if (element == root_) {
            if (!configuration.isInitialized()) {
                return new Object[]{BldBundle.message("bld.progress.text.loading.config")};
            }
            return new Object[]{commandsFolder_, dependenciesFolder_};
        }

        if (element == commandsFolder_) {
            return configuration.getCommands().toArray(new BldBuildCommand[0]);
        }

        if (element == dependenciesFolder_) {
            return configuration.getDependencyTree().toArray(new BldDependencyNode[0]);
        }

        if (element instanceof BldDependencyNode node) {
            return node.children().toArray(new BldDependencyNode[0]);
        }

        return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
    }

    @Override
    @Nullable
    public Object getParentElement(@NotNull Object element) {
        if (element instanceof BldBuildCommand) {
            return commandsFolder_;
        }

        if (element == commandsFolder_) {
            return root_;
        }

        if (element == dependenciesFolder_) {
            return root_;
        }

        if (element instanceof BldDependencyNode node) {
            if (node.parent() == null) {
                return dependenciesFolder_;
            }
            return node.parent();
        }

        return null;
    }

    @Override
    public void commit() {
        PsiDocumentManager.getInstance(project_).commitAllDocuments();
    }

    @Override
    public boolean hasSomethingToCommit() {
        return PsiDocumentManager.getInstance(project_).hasUncommitedDocuments();
    }

    @NotNull
    @Override
    public ActionCallback asyncCommit() {
        return asyncCommitDocuments(project_);
    }

    @NotNull
    @Override
    public Object getRootElement() {
        return root_;
    }
}
