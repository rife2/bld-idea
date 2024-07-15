package rife.bld.idea.service.project.wizard;


import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectJdkStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.externalSystem.service.project.wizard.AbstractExternalProjectImportProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.utils.BldBundle;
import rife.bld.idea.utils.BldConstants;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class BldProjectImportProvider extends AbstractExternalProjectImportProvider {
    private static final boolean CAN_BE_CANCELLED = true;

    static public String label() {
        return Optional
            .ofNullable(PropertiesComponent.getInstance().getValue("pants.import.provider.label"))
            .orElse("Bld");
    }

    public BldProjectImportProvider() {
        super(BldProjectImportBuilder.getInstance(), BldConstants.SYSTEM_ID);
    }

    @Override
    public @NotNull
    @Nls(capitalization = Nls.Capitalization.Sentence) String getName() {
        return label();
    }

    @Override
    protected boolean canImportFromFile(VirtualFile file) {
        return BldUtil.isBUILDFileName(file.getName()) || BldUtil.isExecutable(file.getCanonicalPath());
    }

    @Nullable
    @Override
    public String getFileSample() {
        return "<b>Bld</b> build file (BUILD.*) or a script";
    }

    @Override
    public ModuleWizardStep[] createSteps(WizardContext context) {
        /**
         * Newer export version project sdk can be automatically discovered and configured.
         */
        AtomicBoolean isSdkConfigured = new AtomicBoolean(true);
        String message = BldBundle.message("pants.default.sdk.config.progress");
        ProgressManager.getInstance().run(new Task.Modal(context.getProject(), message, !CAN_BE_CANCELLED) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                if (isSdkConfigured.get()) {
                    isSdkConfigured.set(isJvmProject(context.getProjectFileDirectory()));
                }
            }
        });
        if (isSdkConfigured.get()) {
            return super.createSteps(context);
        }
        return ArrayUtil.append(super.createSteps(context), new ProjectJdkStep(context));
    }

    // TODO check for Python project to python interpreter automatically.
    // https://github.com/pantsbuild/intellij-pants-plugin/issues/128
    private boolean isJvmProject(String rootPath) {
        if (BldUtil.isExecutable(rootPath)) {
            return false;
        }

        try {
            return ProjectType.Jvm == SimpleProjectTypeDetector.create(new File(rootPath)).detect();
        } catch (IOException ex) {
            throw new BldException(String.format("Failed detecting project type for %s", rootPath));
        }
    }
}
