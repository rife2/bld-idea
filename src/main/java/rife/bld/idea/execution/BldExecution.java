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
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import rife.bld.idea.config.BldBuildCommand;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.console.BldConsoleManager;
import rife.bld.idea.utils.BldConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import static rife.bld.idea.utils.BldConstants.*;

@Service(Service.Level.PROJECT)
public final class BldExecution {
    private final Project project_;
    private final ConcurrentHashMap<Project, Process> runningBldProcesses_ = new ConcurrentHashMap<>();

    private VirtualFile projectDir_ = null;
    private String bldMainClass_ = null;

    private boolean offline_ = false;

    public BldExecution(@NotNull Project project) {
        project_ =  project;
    }

    public static BldExecution getInstance(final @NotNull Project project) {
        return project.getService(BldExecution.class);
    }

    public void setOffline(boolean flag) {
        offline_ = flag;
    }

    public boolean isOffline() {
        return offline_;
    }

    public boolean hasActiveBldProcess() {
        return runningBldProcesses_.containsKey(project_);
    }

    public void terminateBldProcess() {
        var process = runningBldProcesses_.get(project_);
        if (process != null) {
            process.destroy();
            runningBldProcesses_.remove(project_, process);
        }
    }

    public String getBldMainClass() {
        return bldMainClass_;
    }

    public VirtualFile getBldProperties() {
        var lib = projectDir_.findChild("lib");
        if (lib == null) return null;
        var bld = lib.findChild("bld");
        if (bld == null) return null;
        return bld.findChild("bld-wrapper.properties");
    }

    public boolean hasBldProperties() {
        return getBldProperties() != null;
    }

    public void setupProject() {
        projectDir_ = ProjectUtil.guessProjectDir(project_);
        if (projectDir_ == null) {
            BldConsoleManager.showTaskMessage("Could not find project directory\n", ConsoleViewContentType.ERROR_OUTPUT, project_);
            return;
        }

        bldMainClass_ = guessBldMainClass(projectDir_);
        if (bldMainClass_ == null) {
            BldConsoleManager.showTaskMessage("Could not find bld main class for project " + projectDir_ + "\n", ConsoleViewContentType.ERROR_OUTPUT, project_);
            return;
        }

        BldConsoleManager.showTaskMessage("Found bld main class: " + bldMainClass_ + "\n", ConsoleViewContentType.SYSTEM_OUTPUT, project_);

        BldConfiguration.getInstance(project_).setupComplete();
    }

    public void listTasks() {
        var output = String.join("", executeCommands(true, "help", WRAPPER_JSON_ARGUMENT));
        if (output.isEmpty()) {
            BldConsoleManager.showTaskMessage("Failed to detect the bld commands.\n", ConsoleViewContentType.ERROR_OUTPUT, project_);
            return;
        }
        
        BldConsoleManager.showTaskMessage("Detected the bld commands\n", ConsoleViewContentType.SYSTEM_OUTPUT, project_);

        var commands = new ArrayList<BldBuildCommand>();

        try {
            var json = new JSONObject(output);
            var json_commands = json.getJSONObject("commands");
            for (var json_command_key : json_commands.keySet()) {
                commands.add(new BldBuildCommand(json_command_key, json_command_key, json_commands.getString(json_command_key)));
            }
        } catch (JSONException e) {
            BldConsoleManager.showTaskMessage(output + "\n", ConsoleViewContentType.ERROR_OUTPUT, project_);
        }

        BldConfiguration.getInstance(project_).setBuildCommandList(commands);
    }

    public List<String> executeCommands(boolean captureJson, String... commands) {
        return executeCommands(captureJson, Arrays.asList(commands));
    }

    public List<String> executeCommands(boolean captureJson, List<String> commands) {
        if (projectDir_ == null || bldMainClass_ == null) {
            return Collections.emptyList();
        }

        final var command_line = new GeneralCommandLine();
        command_line.setWorkDirectory(projectDir_.getCanonicalPath());
        command_line.setExePath("java");
        command_line.addParameter("-jar");
        command_line.addParameter(projectDir_.getCanonicalPath() + "/lib/bld/bld-wrapper.jar");
        command_line.addParameter(projectDir_.getCanonicalPath() + "/bld");
        command_line.addParameter(WRAPPER_BUILD_ARGUMENT);
        command_line.addParameter(bldMainClass_);
        if (offline_) {
            command_line.addParameters(WRAPPER_OFFLINE_ARGUMENT);
        }
        if (commands != null) {
            command_line.addParameters(commands);
        }

        final Process process;
        try {
            process = command_line.createProcess();
        }
        catch (ExecutionException e) {
            BldConsoleManager.showTaskMessage(e.getMessage() != null ? e.getMessage() : e.toString(), ConsoleViewContentType.ERROR_OUTPUT, project_);
            return Collections.emptyList();
        }

        final var process_handler = new CapturingAnsiEscapesAwareProcessHandler(process, command_line.getCommandLineString());
        final var output = new ArrayList<String>();
        process_handler.addProcessListener(new ProcessAdapter() {
            boolean jsonStarted_ = false;

            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                if (!outputType.equals(ProcessOutputType.SYSTEM)) {
                    var text = event.getText();
                    if (captureJson && !jsonStarted_) {
                        if (text.trim().startsWith("{")) {
                            jsonStarted_ = true;
                        }
                    }
                    if (!jsonStarted_) {
                        BldConsoleManager.showTaskMessage(event.getText(), ConsoleViewContentType.NORMAL_OUTPUT, project_);
                    }
                    if (!captureJson || jsonStarted_) {
                        output.add(event.getText());
                    }
                }
            }
        });

        runningBldProcesses_.put(project_, process);
        process_handler.runProcess();
        runningBldProcesses_.remove(project_, process);

        return output;
    }

    private String guessBldMainClass(@NotNull VirtualFile projectDir) {
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
                BldConsoleManager.showTaskMessage("Unable to read contents of " + project_bld + "\n", ConsoleViewContentType.ERROR_OUTPUT, project_);
            }
        }
        return null;
    }

}
