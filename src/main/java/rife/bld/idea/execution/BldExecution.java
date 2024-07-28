/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.SimpleJavaParameters;
import com.intellij.execution.process.*;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.config.BldBuildCommand;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.console.BldConsoleManager;
import rife.bld.idea.utils.BldConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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

    public static BldExecution instance(final @NotNull Project project) {
        return project.getService(BldExecution.class);
    }

    public Project project() {
        return project_;
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
            process.descendants().forEach(ProcessHandle::destroy);

            process.destroy();
            try {
                process.getInputStream().close();
            } catch (IOException e) {
                // no-op
            }
            try {
                process.getOutputStream().close();
            } catch (IOException e) {
                // no-op
            }
            try {
                process.getErrorStream().close();
            } catch (IOException e) {
                // no-op
            }

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

    public VirtualFile getBldCache() {
        var lib = projectDir_.findChild("lib");
        if (lib == null) return null;
        var bld = lib.findChild("bld");
        if (bld == null) return null;
        return bld.findChild("bld.cache");
    }

    public boolean hasBldCache() {
        return getBldCache() != null;
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

        BldConfiguration.instance(project_).setupComplete();
    }

    public List<String> executeCommands(BldExecutionFlags flags, String command) {
        return executeCommands(flags, command, BldBuildListener.DUMMY);
    }

    public List<String> executeCommands(BldExecutionFlags flags, String command, BldBuildListener listener) {
        return executeCommands(flags, List.of(command), listener);
    }

    public List<String> executeCommands(BldExecutionFlags flags, BldBuildCommand command, BldBuildListener listener) {
        return executeCommands(flags, List.of(command.name()), listener);
    }

    public List<String> executeCommands(BldExecutionFlags flags, List<String> commands) {
        return executeCommands(flags, commands, BldBuildListener.DUMMY);
    }

    public List<String> executeCommands(BldExecutionFlags flags, List<String> commands, BldBuildListener listener) {
        var process_handler = createProcessHandler(commands, Collections.emptyList(), listener);
        if (process_handler == null) {
            return Collections.emptyList();
        }

        final var output = new ArrayList<String>();
        process_handler.addProcessListener(new ProcessAdapter() {
            boolean jsonStarted_ = false;

            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                super.onTextAvailable(event, outputType);
                if (!outputType.equals(ProcessOutputType.SYSTEM)) {
                    var text = event.getText();
                    if (flags.commands() && !jsonStarted_) {
                        if (text.trim().startsWith("{")) {
                            jsonStarted_ = true;
                        }
                    }
                    if (!jsonStarted_ && !flags.dependencyTree()) {
                        BldConsoleManager.showTaskMessage(event.getText(), ConsoleViewContentType.NORMAL_OUTPUT, project_);
                    }

                    if (!flags.commands() || jsonStarted_) {
                        output.add(event.getText());
                    }
                }
            }
        });

        runningBldProcesses_.put(project_, process_handler.getProcess());
        try {
            process_handler.runProcess();
        }
        finally {
            runningBldProcesses_.remove(project_, process_handler.getProcess());

            listener.buildFinished(BldBuildListener.FINISHED_SUCCESSFULLY);
            projectDir_.refresh(true, true);
        }

        return output;
    }

    public CapturingProcessHandler createProcessHandler(List<String> commands, List<BldRunProperty> properties, BldBuildListener listener) {
        if (projectDir_ == null || bldMainClass_ == null) {
            listener.buildFinished(BldBuildListener.FAILED_TO_RUN);
            return null;
        }

        var java_parameters = new SimpleJavaParameters();
        java_parameters.setJdk(ProjectRootManager.getInstance(project_).getProjectSdk());
        java_parameters.setWorkingDirectory(projectDir_.getCanonicalPath());
        java_parameters.setJarPath(projectDir_.getCanonicalPath() + "/lib/bld/bld-wrapper.jar");

        var jvm_parameters = java_parameters.getVMParametersList();
        for (var property : properties) {
            jvm_parameters.defineProperty(property.getPropertyName(), property.getPropertyValue());
        }

        var program_parameters = java_parameters.getProgramParametersList();
        program_parameters.add(projectDir_.getCanonicalPath() + "/bld");
        program_parameters.add(WRAPPER_BUILD_ARGUMENT);
        program_parameters.add(bldMainClass_);
        if (offline_) {
            program_parameters.add(WRAPPER_OFFLINE_ARGUMENT);
        }
        if (commands != null) {
            program_parameters.addAll(commands);
        }

        GeneralCommandLine command_line;
        try {
            command_line = java_parameters.toCommandLine();
        } catch (CantRunException e) {
            BldConsoleManager.showTaskMessage(e.getMessage() != null ? e.getMessage() + "\n" : e.toString(), ConsoleViewContentType.ERROR_OUTPUT, project_);
            listener.buildFinished(BldBuildListener.FAILED_TO_RUN);
            return null;
        }

        final Process process;
        try {
            process = command_line.createProcess();
        }
        catch (ExecutionException e) {
            BldConsoleManager.showTaskMessage(e.getMessage() != null ? e.getMessage() + "\n" : e.toString(), ConsoleViewContentType.ERROR_OUTPUT, project_);
            listener.buildFinished(BldBuildListener.FAILED_TO_RUN);
            return null;
        }

        return new CapturingAnsiEscapesAwareProcessHandler(process, command_line.getCommandLineString());
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
