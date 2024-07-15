package rife.bld.idea.service.settings;


import com.intellij.openapi.externalSystem.service.settings.AbstractImportFromExternalSystemControl;
import com.intellij.openapi.externalSystem.util.ExternalSystemSettingsControl;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.settings.BldProjectSettings;
import rife.bld.idea.settings.BldSettings;
import rife.bld.idea.settings.BldSettingsListener;
import rife.bld.idea.utils.BldConstants;

public class ImportFromBldControl
    extends AbstractImportFromExternalSystemControl<BldProjectSettings, BldSettingsListener, BldSettings> {

    public ImportFromBldControl() {
        super(BldConstants.SYSTEM_ID, BldSettings.defaultSettings(), getInitialProjectSettings(), true);
    }

    @NotNull
    private static BldProjectSettings getInitialProjectSettings() {
        return new BldProjectSettings();
    }

    @Override
    protected void onLinkedProjectPathChange(@NotNull String path) {
        if (!StringUtil.isEmpty(path)) {
            ((BldProjectSettingsControl)getProjectSettingsControl()).onProjectPathChanged(path);
        }
    }

    @NotNull
    @Override
    protected ExternalSystemSettingsControl<BldProjectSettings> createProjectSettingsControl(@NotNull BldProjectSettings settings) {
        return new BldProjectSettingsControl(settings);
    }

    @Nullable
    @Override
    protected ExternalSystemSettingsControl<BldSettings> createSystemSettingsControl(@NotNull BldSettings settings) {
        return null;
    }
}
