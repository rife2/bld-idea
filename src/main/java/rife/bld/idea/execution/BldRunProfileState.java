/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.psi.search.ExecutionSearchScopes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.console.BldConsoleManager;
import rife.bld.idea.utils.BldBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BldRunProfileState implements RunProfileState {
    private static final Logger LOG = Logger.getInstance(BldRunProfileState.class);

    private final ExecutionEnvironment environment_;
    private TextConsoleBuilder consoleBuilder_;

    BldRunProfileState(ExecutionEnvironment environment) {
        environment_ = environment;
        if (environment_ != null) {
            var project = environment_.getProject();
            var scope = ExecutionSearchScopes.executionScope(project, environment_.getRunProfile());
            consoleBuilder_ = TextConsoleBuilderFactory.getInstance().createBuilder(project, scope);
        }
    }

    @Nullable
    @Override
    public ExecutionResult execute(Executor executor, @NotNull ProgramRunner<?> runner) {
        final var profile = environment_.getRunProfile();
        if (profile instanceof BldRunConfiguration runConfig) {
            if (runConfig.getCommand() == null) {
                return null;
            }

            final var console = consoleBuilder_.getConsole();
            console.addMessageFilter(BldConsoleManager.createMessageFilter(environment_.getProject()));

            final var future = new CompletableFuture<ProcessHandler>();
            var task = new Task.Backgroundable(null, BldBundle.message("bld.build.progress.dialog.title"), true) {
                public void run(@NotNull ProgressIndicator indicator) {
                    var commands = new ArrayList<>(List.of(runConfig.getCommand().name()));
                    commands.addAll(runConfig.getRunOptions().stream().map(BldRunOption::getOptionName).toList());
                    var handler = BldExecution.instance(environment_.getProject())
                        .createProcessHandler(
                            commands,
                            runConfig.getRunProperties(),
                            BldBuildListener.DUMMY);
                    if (handler != null) {
                        console.attachToProcess(handler);

                        handler.startNotify();
                    }
                    future.complete(handler);
                }
            };
            task.queue();

            ProcessHandler processHandler;
            try {
                processHandler = future.get();
            } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                LOG.warn(e);
                return null;
            }

            return new DefaultExecutionResult(console, processHandler);
        }
        return null;
    }
}