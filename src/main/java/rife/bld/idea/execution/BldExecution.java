/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingAnsiEscapesAwareProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.config.BldBuildCommand;
import rife.bld.idea.config.BldBuildListener;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.console.BldConsoleManager;
import rife.bld.idea.utils.BldConstants;
import rife.bld.wrapper.Wrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

public class BldExecution {
    private static final ConcurrentHashMap<Project, Process> runningBldProcesses_ = new ConcurrentHashMap<>();

    public BldExecution(@NotNull Project project) {
    }

    public static boolean hasActiveBldProcess(@NotNull Project project) {
        return runningBldProcesses_.containsKey(project);
    }

    public static void terminateBldProcess(Project project) {
        var process = runningBldProcesses_.get(project);
        if (process != null) {
            process.destroy();
            runningBldProcesses_.remove(project, process);
        }
    }

    public static void listTasks(@NotNull Project project) {
        var output = String.join("", executeCommands(project, true, "help", Wrapper.JSON_ARGUMENT));
        BldConsoleManager.showTaskMessage("Detected the bld commands\n", ConsoleViewContentType.SYSTEM_OUTPUT, project);

        var commands = new ArrayList<BldBuildCommand>();

        var json = new JSONObject(output);
        var json_commands = json.getJSONObject("commands");
        for (var json_command_key : json_commands.keySet()) {
            commands.add(new BldBuildCommand() {
                @Override
                public @Nullable String getName() {
                    return json_command_key;
                }

                @Override
                public @Nullable String getDisplayName() {
                    return json_command_key;
                }

                @Nls(capitalization = Nls.Capitalization.Sentence)
                @Override
                public @Nullable String getNotEmptyDescription() {
                    return json_commands.getString(json_command_key);
                }

                @Override
                public void run(DataContext dataContext, List<?> additionalProperties, BldBuildListener buildListener) {

                }
            });
        }

        BldConfiguration.getInstance(project).setBuildCommandList(commands);
    }

    public static List<String> executeCommands(@NotNull Project project, boolean silent, String... commands) {
        var project_dir = ProjectUtil.guessProjectDir(project);
        if (project_dir == null) {
            BldConsoleManager.showTaskMessage("Could not find project directory\n", ConsoleViewContentType.ERROR_OUTPUT, project);
            return Collections.emptyList();
        }

        var bldMainClass = guessBldMainClass(project, project_dir);
        if (bldMainClass == null) {
            BldConsoleManager.showTaskMessage("Could not find bld main class for project " + project_dir + "\n", ConsoleViewContentType.ERROR_OUTPUT, project);
            return Collections.emptyList();
        }

        BldConsoleManager.showTaskMessage("Found bld main class: " + bldMainClass + "\n", ConsoleViewContentType.SYSTEM_OUTPUT, project);

        final var command_line = new GeneralCommandLine();
        command_line.setWorkDirectory(project_dir.getCanonicalPath());
        command_line.setExePath("java");
        command_line.addParameter("-jar");
        command_line.addParameter(project_dir.getCanonicalPath() + "/lib/bld/bld-wrapper.jar");
        command_line.addParameter(project_dir.getCanonicalPath() + "/bld");
        command_line.addParameter(Wrapper.BUILD_ARGUMENT);
        command_line.addParameter(bldMainClass);
        if (commands != null) {
            command_line.addParameters(commands);
        }

        final Process process;
        try {
            process = command_line.createProcess();
        }
        catch (ExecutionException e) {
            BldConsoleManager.showTaskMessage(e.getMessage() != null ? e.getMessage() : e.toString(), ConsoleViewContentType.ERROR_OUTPUT, project);
            return Collections.emptyList();
        }

        final var process_handler = new CapturingAnsiEscapesAwareProcessHandler(process, command_line.getCommandLineString());
        final var output = new ArrayList<String>();
        process_handler.addProcessListener(new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                if (!outputType.equals(ProcessOutputType.SYSTEM)) {
                    if (!silent) {
                        BldConsoleManager.showTaskMessage(event.getText(), ConsoleViewContentType.NORMAL_OUTPUT, project);
                    }
                    output.add(event.getText());
                }
            }
        });

        runningBldProcesses_.put(project, process);
        process_handler.runProcess();
        runningBldProcesses_.remove(project, process);

        return output;
    }

    private static String guessBldMainClass(@NotNull Project project, @NotNull VirtualFile projectDir) {
        var project_bld = projectDir.findChild("bld");
        if (project_bld == null) {
            project_bld = projectDir.findChild("bld.bat");
        }
        if (project_bld != null) {
            try {
                var contents = new String(project_bld.contentsToByteArray());
                var matcher = BldConstants.BUILD_MAIN_CLASS.matcher(contents);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            } catch (IOException e) {
                BldConsoleManager.showTaskMessage("Unable to read contents of " + project_bld + "\n", ConsoleViewContentType.ERROR_OUTPUT, project);
            }
        }
        return null;
    }

}
