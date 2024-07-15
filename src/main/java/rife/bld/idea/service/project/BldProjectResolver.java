package rife.bld.idea.service.project;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.settings.BldExecutionSettings;

public class BldProjectResolver implements ExternalSystemProjectResolver<BldExecutionSettings> {
    private static final Logger LOG = Logger.getInstance(BldProjectResolver.class);

    @Override
    public boolean cancelTask(@NotNull ExternalSystemTaskId taskId, @NotNull ExternalSystemTaskNotificationListener listener) {
        return false;
    }
}
