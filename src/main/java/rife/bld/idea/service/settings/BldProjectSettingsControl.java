package rife.bld.idea.service.settings;

import com.intellij.openapi.externalSystem.service.settings.AbstractExternalProjectSettingsControl;
import com.intellij.openapi.externalSystem.util.PaintAwarePanel;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.settings.BldProjectSettings;

public class BldProjectSettingsControl extends AbstractExternalProjectSettingsControl<BldProjectSettings> {
    public BldProjectSettingsControl(@NotNull BldProjectSettings initialSettings) {
        super(initialSettings);
    }

    public BldProjectSettingsControl(@Nullable Project project, @NotNull BldProjectSettings initialSettings) {
        super(project, initialSettings);
    }

    @Override
    protected void fillExtraControls(@NotNull PaintAwarePanel content, int indentLevel) {
    }

    @Override
    protected boolean isExtraSettingModified() {
        return false;
    }

    @Override
    protected void resetExtraSettings(boolean isDefaultModuleCreation) {
    }

    @Override
    protected void applyExtraSettings(@NotNull BldProjectSettings settings) {
    }

    @Override
    public boolean validate(@NotNull BldProjectSettings settings)
    throws ConfigurationException {
        return false;
    }
}
