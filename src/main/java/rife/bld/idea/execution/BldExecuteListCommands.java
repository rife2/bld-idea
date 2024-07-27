/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import com.intellij.execution.ui.ConsoleViewContentType;
import org.json.JSONException;
import org.json.JSONObject;
import rife.bld.idea.config.BldBuildCommand;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.console.BldConsoleManager;

import java.util.ArrayList;
import java.util.List;

import static rife.bld.idea.utils.BldConstants.WRAPPER_JSON_ARGUMENT;

public abstract class BldExecuteListCommands {
    public static void run(BldExecution execution) {
        var output = String.join("", execution.executeCommands(new BldExecutionFlags().commands(true), List.of("help", WRAPPER_JSON_ARGUMENT)));
        if (output.isEmpty()) {
            BldConsoleManager.showTaskMessage("Failed to detect the bld commands.\n", ConsoleViewContentType.ERROR_OUTPUT, execution.project());
            return;
        }

        BldConsoleManager.showTaskMessage("Detected the bld commands\n", ConsoleViewContentType.SYSTEM_OUTPUT, execution.project());

        var commands = new ArrayList<BldBuildCommand>();

        try {
            var json = new JSONObject(output);
            var json_commands = json.getJSONObject("commands");
            for (var json_command_key : json_commands.keySet()) {
                commands.add(new BldBuildCommand(json_command_key, json_command_key, json_commands.getString(json_command_key)));
            }
        } catch (JSONException e) {
            BldConsoleManager.showTaskMessage(output + "\n", ConsoleViewContentType.ERROR_OUTPUT, execution.project());
        }

        BldConfiguration.instance(execution.project()).setCommands(commands);
    }
}
