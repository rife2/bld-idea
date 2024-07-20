/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import com.intellij.execution.ui.ConsoleViewContentType;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.console.BldConsoleManager;

import java.util.regex.Pattern;

public abstract class BldExecuteDependencyTree {
    public static void run(BldExecution execution) {
        var output = execution.executeCommands(new BldExecutionFlags().dependencyTree(true), "dependency-tree");
        if (output.isEmpty()) {
            BldConsoleManager.showTaskMessage("Failed to calculate the dependency tree.\n", ConsoleViewContentType.ERROR_OUTPUT, execution.project());
            return;
        }

        var tree = new BldDependencyTree();
        BldDependencyNode current_node = null;
        var current_depth = 0;
        for (var line : output) {
            line = line.trim();

            if (line.startsWith("extensions:")) {
                current_node = tree.extensions;
                current_depth = 1;
            } else if (line.startsWith("compile:")) {
                current_node = tree.compile;
                current_depth = 1;
            } else if (line.startsWith("provided:")) {
                current_node = tree.provided;
                current_depth = 1;
            } else if (line.startsWith("runtime:")) {
                current_node = tree.runtime;
                current_depth = 1;
            } else if (line.startsWith("test:")) {
                current_node = tree.test;
                current_depth = 1;
            }
            else if (!line.isEmpty() && current_node != null) {
                var pattern = Pattern.compile("\\w");
                var matcher = pattern.matcher(line);
                if (matcher.find()) {
                    var index = matcher.start();
                    if (index > 0) {
                        var depth = index / 3;
                        var dependency = line.substring(index);
                        if (depth > current_depth) {
                            current_depth = depth;
                            current_node = current_node.children().get(current_node.children().size() - 1);
                        }
                        else if (depth < current_depth) {
                            while (depth < current_depth && current_node != null) {
                                current_depth -= 1;
                                current_node = current_node.parent();
                            }
                        }

                        if (current_node != null) {
                            current_node.addChild(new BldDependencyNode(dependency));
                        }
                    }
                }
            }
        }

        BldConfiguration.getInstance(execution.project()).setDependencyTree(tree);

        BldConsoleManager.showTaskMessage("Detected the dependency tree\n", ConsoleViewContentType.SYSTEM_OUTPUT, execution.project());
    }
}
