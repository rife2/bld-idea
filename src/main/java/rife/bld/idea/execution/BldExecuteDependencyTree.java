/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import com.intellij.execution.ui.ConsoleViewContentType;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.console.BldConsoleManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public abstract class BldExecuteDependencyTree {
    private static final Pattern FIRST_WORD_CHARACTER = Pattern.compile("\\w");

    public static void run(BldExecution execution) {
        var succeeded = new AtomicBoolean(false);
        var output = execution.executeCommands(new BldExecutionFlags().dependencyTree(true), "dependency-tree",
            state -> succeeded.set(state == BldBuildListener.FINISHED_SUCCESSFULLY));
        if (!succeeded.get() || output.isEmpty()) {
            // the output of this command isn't echoed, so what went wrong is only visible here
            BldConsoleManager.showTaskMessage(String.join("", output), ConsoleViewContentType.ERROR_OUTPUT, execution.project());
            BldConsoleManager.showTaskMessage("Failed to calculate the dependency tree.\n", ConsoleViewContentType.ERROR_OUTPUT, execution.project());
            return;
        }

        BldConfiguration.instance(execution.project()).setDependencyTree(parse(output));

        BldConsoleManager.showTaskMessage("Detected the dependency tree\n", ConsoleViewContentType.SYSTEM_OUTPUT, execution.project());
    }

    static BldDependencyTree parse(List<String> output) {
        var tree = new BldDependencyTree();
        BldDependencyNode current_node = null;
        var current_depth = 0;
        for (var line : output) {
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
                var matcher = FIRST_WORD_CHARACTER.matcher(line);
                if (matcher.find()) {
                    var index = matcher.start();
                    if (index > 0) {
                        var depth = index / 3;
                        // the process output arrives line by line, with the line ending
                        var dependency = line.substring(index).strip();
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

        return tree;
    }
}
