package rife.bld.idea.toolWindow;

import com.intellij.openapi.externalSystem.service.task.ui.AbstractExternalSystemToolWindowFactory;
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.settings.BldSettings;
import rife.bld.idea.utils.BldConstants;

final class BldToolWindowFactory extends AbstractExternalSystemToolWindowFactory {
    BldToolWindowFactory() {
        super(BldConstants.SYSTEM_ID);
    }

    @Override
    protected @NotNull AbstractExternalSystemSettings<?, ?, ?> getSettings(@NotNull Project project) {
        return BldSettings.getInstance(project);
    }
}
