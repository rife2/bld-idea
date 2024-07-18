/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.console;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.utils.BldPluginDisposable;

import java.util.Objects;
import java.util.Optional;

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

    public static void showTaskMessage(@NotNull String message, @NotNull ConsoleViewContentType type,
                                       @NotNull Project project) {
        var executionConsole = BldConsoleManager.getConsole(project);
        // Create a filter that monitors console outputs, and turns them into a hyperlink if applicable.
        Filter filter = (line, entireLength) -> {
            Optional<ParseResult> result = ParseResult.parseErrorLocation(line);
            if (result.isPresent()) {
                var linkInfo = new OpenFileHyperlinkInfo(
                        project,
                        result.get().file,
                        result.get().lineNumber - 1, // line number is 0 indexed
                        result.get().columnNumber // column number is 0 indexed
                );

                return new Filter.Result(
                        entireLength - line.length(),
                        entireLength - (line.length() - result.get().linkHighlightLength),
                        linkInfo,
                        null // default TextAttributes
                );
            }
            return null;
        };

        ApplicationManager.getApplication().invokeLater(() -> {
            executionConsole.addMessageFilter(filter);
            executionConsole.print(message, type);
        }, ModalityState.nonModal());
    }

    public ConsoleView getConsole() {
        return bldConsole_;
    }

    /**
     * Parses the result data.
     */
    static class ParseResult {
        private final int columnNumber;
        private final VirtualFile file;
        private final int lineNumber;
        private final int linkHighlightLength;

        private ParseResult(VirtualFile file, int lineNumber, int columnNumber, int linkHighlightLength) {
            this.file = file;
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
            this.linkHighlightLength = linkHighlightLength;
        }

        /**
         * Parses the line for errors.
         *
         * @param line the output line
         * @return an optional {@link ParseResult} instance
         */
        public static Optional<ParseResult> parseErrorLocation(String line) {
            // matching lines starting with: /path/to/file:lineNumber:columNumber:...
            if (!line.trim().matches(".*:\\d+:.*")) {
                return Optional.empty();
            }

            var splitLine = line.split(":");
            if (splitLine.length < 3) {
                return Optional.empty();
            }

            try {
                // file path
                var virtualFile = LocalFileSystem.getInstance().findFileByPath(splitLine[0]);
                if (virtualFile == null) {
                    return Optional.empty();
                }

                // line number
                var lineNumber = Integer.parseInt(splitLine[1]);

                // path/to/file + : + lineNumber + :
                var linkHighlightLength = splitLine[0].length() + 1 + splitLine[1].length() + 1;

                // column number
                int columnNumber = 0;
                try {
                    columnNumber = Integer.parseInt(splitLine[2]);
                    linkHighlightLength += splitLine[2].length() + 1;
                } catch (NumberFormatException ignore) {
                    // no column number
                }

                return Optional.of(new ParseResult(virtualFile, lineNumber, columnNumber, linkHighlightLength));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            var other = (ParseResult) obj;
            return Objects.equals(file, other.file)
                    && Objects.equals(lineNumber, other.lineNumber)
                    && Objects.equals(linkHighlightLength, other.linkHighlightLength);
        }
    }
}
