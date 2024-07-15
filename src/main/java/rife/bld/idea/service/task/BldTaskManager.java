package rife.bld.idea.service.task;

import com.intellij.openapi.externalSystem.model.ExternalSystemException;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.settings.BldExecutionSettings;

public class BldTaskManager implements ExternalSystemTaskManager<BldExecutionSettings> {
    @Override
    public boolean cancelTask(@NotNull ExternalSystemTaskId id, @NotNull ExternalSystemTaskNotificationListener listener)
    throws ExternalSystemException {
        return false;
    }
}
