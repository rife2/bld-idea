package rife.bld.idea.execution;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTask;
import com.intellij.openapi.externalSystem.service.execution.DefaultExternalSystemExecutionConsoleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.utils.BldConstants;

public class BldExecutionConsoleManager extends DefaultExternalSystemExecutionConsoleManager {
    @Override
    public @NotNull ProjectSystemId getExternalSystemId() {
        return BldConstants.SYSTEM_ID;
    }

    @Nullable
    @Override
    public ExecutionConsole attachExecutionConsole(@NotNull Project project,
                                                   @NotNull ExternalSystemTask task,
                                                   @Nullable ExecutionEnvironment env,
                                                   @Nullable ProcessHandler processHandler) {
        ConsoleView executionConsole = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        executionConsole.attachToProcess(processHandler);
        Filter[] filters = getCustomExecutionFilters(project, task, env);
        for (Filter filter : filters) {
            executionConsole.addMessageFilter(filter);
        }
        return executionConsole;
    }

    @Override
    public boolean isApplicableFor(@NotNull ExternalSystemTask task) {
        return BldConstants.SYSTEM_ID.equals(task.getId().getProjectSystemId());
    }
}
