package rife.bld.idea.settings;

import com.intellij.openapi.externalSystem.service.settings.AbstractExternalSystemConfigurable;
import com.intellij.openapi.externalSystem.util.ExternalSystemSettingsControl;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.service.settings.BldProjectSettingsControl;
import rife.bld.idea.service.settings.BldSystemSettingsControl;
import rife.bld.idea.utils.BldConstants;

public final class BldConfigurable extends AbstractExternalSystemConfigurable<BldProjectSettings, BldSettingsListener, BldSettings> {

    public static final String DISPLAY_NAME = BldConstants.SYSTEM_ID.getReadableName();
    public static final String ID = "reference.settingsdialog.project.gradle";
    @NonNls
    public static final String HELP_TOPIC = ID;

    public BldConfigurable(@NotNull Project project) {
        super(project, BldConstants.SYSTEM_ID);
    }

    @NotNull
    @Override
    protected ExternalSystemSettingsControl<BldProjectSettings> createProjectSettingsControl(@NotNull BldProjectSettings settings) {
        return new BldProjectSettingsControl(settings);
    }

    @Override
    protected ExternalSystemSettingsControl<BldSettings> createSystemSettingsControl(@NotNull BldSettings settings) {
        return new BldSystemSettingsControl(settings);
    }

    @NotNull
    @Override
    protected BldProjectSettings newProjectSettings() {
        return new BldProjectSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @NotNull
    @Override
    public String getHelpTopic() {
        return HELP_TOPIC;
    }
}
