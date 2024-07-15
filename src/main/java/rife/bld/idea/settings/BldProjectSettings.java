package rife.bld.idea.settings;

import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings;
import org.jetbrains.annotations.NotNull;

public class BldProjectSettings extends ExternalProjectSettings {
    @Override
    public @NotNull ExternalProjectSettings clone() {
        var result = new BldProjectSettings();
        return result;
    }
}