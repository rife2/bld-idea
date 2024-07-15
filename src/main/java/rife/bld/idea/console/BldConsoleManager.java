/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.console;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.utils.BldPluginDisposable;

@Service(Service.Level.PROJECT)
public final class BldConsoleManager {
    private final ConsoleView bldConsole_;

    public BldConsoleManager(@NotNull Project project) {
        bldConsole_ = createNewConsole(project);
        Disposer.register(BldPluginDisposable.getInstance(project), bldConsole_);
    }

    private static ConsoleView createNewConsole(@NotNull Project project) {
        return TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
    }

    public static ConsoleView getConsole(@NotNull Project project) {
        var service = project.getService(BldConsoleManager.class);
        return service.getConsole();
    }

    public ConsoleView getConsole() {
        return bldConsole_;
    }

    public static void showTaskMessage(@NotNull String message, @NotNull ConsoleViewContentType type, @NotNull Project project) {
        var executionConsole = BldConsoleManager.getConsole(project);
        // Create a filter that monitors console outputs, and turns them into a hyperlink if applicable.
        Filter filter = (line, entireLength) -> {
//            Optional<ParseResult> result = ParseResult.parseErrorLocation(line, ERROR_TAG);
//            if (result.isPresent()) {
//
//                OpenFileHyperlinkInfo linkInfo = new OpenFileHyperlinkInfo(
//                    project,
//                    result.get().getFile(),
//                    result.get().getLineNumber() - 1, // line number needs to be 0 indexed
//                    result.get().getColumnNumber() - 1 // column number needs to be 0 indexed
//                );
//                int startHyperlink = entireLength - line.length() + line.indexOf(ERROR_TAG);
//
//                return new Filter.Result(
//                    startHyperlink,
//                    entireLength,
//                    linkInfo,
//                    null // TextAttributes, going with default hence null
//                );
//            }
            return null;
        };

        ApplicationManager.getApplication().invokeLater(() -> {
            executionConsole.addMessageFilter(filter);
            executionConsole.print(message, type);
        }, ModalityState.nonModal());
    }
}
