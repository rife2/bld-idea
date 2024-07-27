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

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Service(Service.Level.PROJECT)
public final class BldConsoleManager {
    private final ConsoleView bldConsole_;

    public BldConsoleManager(@NotNull Project project) {
        bldConsole_ = createNewConsole(project);
        bldConsole_.addMessageFilter(createMessageFilter(project));
        Disposer.register(BldPluginDisposable.instance(project), bldConsole_);
    }

    public static ConsoleView createNewConsole(@NotNull Project project) {
        return TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
    }

    public static ConsoleView getConsole(@NotNull Project project) {
        var service = project.getService(BldConsoleManager.class);
        return service.getConsole();
    }

    public static void showTaskMessage(@NotNull String message, @NotNull ConsoleViewContentType type,
                                       @NotNull Project project) {
        var executionConsole = BldConsoleManager.getConsole(project);
        ApplicationManager.getApplication().invokeLater(() -> {
            executionConsole.print(message, type);
        }, ModalityState.nonModal());
    }

    // Create a filter that monitors console outputs, and turns them into a hyperlink if applicable.
    public static @NotNull Filter createMessageFilter(@NotNull Project project) {
        return (line, entireLength) -> {
            Optional<ParseResult> result = ParseResult.parseErrorLocation(project, line);
            if (result.isPresent()) {
                var linkInfo = new OpenFileHyperlinkInfo(
                    project,
                        result.get().file,
                        result.get().lineNumber - 1, // 0 indexed
                        result.get().columnNumber - 1 // 0 indexed
                );

                var startLineOffset = line.length() - result.get().linkHighlightStart;

                return new Filter.Result(
                        entireLength - startLineOffset,
                        entireLength - (startLineOffset - result.get().linkHighlightLength),
                        linkInfo,
                        null // default TextAttributes
                );
            }
            return null;
        };
    }

    public ConsoleView getConsole() {
        return bldConsole_;
    }

    /**
     * Parses the result data.
     */
    static class ParseResult {
        private static final Pattern MATCH_PATTERN = Pattern.compile(".*:\\d+:.*");
        private final int columnNumber;
        private final VirtualFile file;
        private final int lineNumber;
        private final int linkHighlightLength;
        private final int linkHighlightStart;

        private ParseResult(VirtualFile file, int lineNumber, int columnNumber, int linkHighlightStart,
                            int linkHighlightLength) {
            this.file = file;
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
            this.linkHighlightStart = linkHighlightStart;
            this.linkHighlightLength = linkHighlightLength;
        }

        /**
         * Parses the line for errors.
         *
         * @param project the project
         * @param line    the output line
         * @return an optional {@link ParseResult} instance
         */
        public static Optional<ParseResult> parseErrorLocation(Project project, String line) {
            // matching lines containing: /path/to/file:lineNumber:
            var matcher = MATCH_PATTERN.matcher(line.trim());
            if (!matcher.matches()) {
                return Optional.empty();
            }

            var splitBySpace = line.split(" ");
            if (splitBySpace.length < 3) {
                return Optional.empty();
            }

            for (var splits : splitBySpace) {
                matcher.reset(splits);
                if (matcher.matches()) {
                    var splitByColon = splits.split(":");
                    // file path
                    var file = new File(splitByColon[0]);
                    if (!file.exists()) {
                        file = new File(project.getBasePath(), splitByColon[0]);
                    }
                    var virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
                    if (virtualFile == null) {
                        return Optional.empty();
                    }

                    var linkHighlightStart = line.indexOf(splits);

                    // line number
                    int lineNumber;
                    try {
                        lineNumber = Integer.parseInt(splitByColon[1]);
                    } catch (NumberFormatException e) {
                        return Optional.empty();
                    }

                    // path/to/file + : + lineNumber
                    var linkHighlightLength = splitByColon[0].length() + 1 + splitByColon[1].length();

                    // column number
                    int columnNumber = 0;
                    if (splitByColon.length >= 3) {
                        try {
                            columnNumber = Integer.parseInt(splitByColon[2]);
                            // : + columNumber
                            linkHighlightLength += 1 + splitByColon[2].length();
                        } catch (NumberFormatException ignore) {
                            // no column number
                        }
                    }

                    return Optional.of(new ParseResult(virtualFile, lineNumber, columnNumber, linkHighlightStart,
                            linkHighlightLength));
                }
            }
            return Optional.empty();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            var other = (ParseResult) obj;
            return Objects.equals(file, other.file)
                    && Objects.equals(lineNumber, other.lineNumber)
                    && Objects.equals(linkHighlightStart, other.linkHighlightStart)
                    && Objects.equals(linkHighlightLength, other.linkHighlightLength);
        }

        @Override
        public String toString() {
            return "ParseResult{" +
                    "file=" + file +
                    ", lineNumber=" + lineNumber +
                    ", columnNumber=" + columnNumber +
                    ", linkHighlightStart=" + linkHighlightStart +
                    ", linkHighlightLength=" + linkHighlightLength +
                    '}';
        }
    }
}
