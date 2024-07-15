package rife.bld.idea.service.settings;

import com.intellij.openapi.externalSystem.util.ExternalSystemSettingsControl;
import com.intellij.openapi.externalSystem.util.PaintAwarePanel;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.settings.BldSettings;

public class BldSystemSettingsControl implements ExternalSystemSettingsControl<BldSettings> {
    public BldSystemSettingsControl(@NotNull BldSettings initialSettings) {
    }

    @Override
    public void fillUi(@NotNull PaintAwarePanel canvas, int indentLevel) {
    }

    @Override
    public void reset() {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply(@NotNull BldSettings settings) {

    }

    @Override
    public boolean validate(@NotNull BldSettings settings)
    throws ConfigurationException {
        return false;
    }

    @Override
    public void disposeUIResources() {

    }

    @Override
    public void showUi(boolean show) {

    }
}
