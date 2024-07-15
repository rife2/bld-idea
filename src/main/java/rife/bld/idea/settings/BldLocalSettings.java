package rife.bld.idea.settings;

import com.intellij.openapi.components.*;
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemLocalSettings;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.utils.BldConstants;

@Service(Service.Level.PROJECT)
@State(name = "BldLocalSettings", storages = @Storage(StoragePathMacros.CACHE_FILE))
public final class BldLocalSettings extends AbstractExternalSystemLocalSettings<AbstractExternalSystemLocalSettings.State>
    implements PersistentStateComponent<AbstractExternalSystemLocalSettings.State> {

    public BldLocalSettings(@NotNull Project project) {
        super(BldConstants.SYSTEM_ID, project);
    }

    @NotNull
    public static BldLocalSettings getInstance(@NotNull Project project) {
        return project.getService(BldLocalSettings.class);
    }
}
