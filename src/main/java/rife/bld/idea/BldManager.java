package rife.bld.idea;

import com.intellij.execution.configurations.SimpleJavaParameters;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.ExternalSystemAutoImportAware;
import com.intellij.openapi.externalSystem.ExternalSystemConfigurableAware;
import com.intellij.openapi.externalSystem.ExternalSystemManager;
import com.intellij.openapi.externalSystem.ExternalSystemUiAware;
import com.intellij.openapi.externalSystem.importing.ProjectResolverPolicy;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver;
import com.intellij.openapi.externalSystem.service.project.autoimport.CachingExternalSystemAutoImportAware;
import com.intellij.openapi.externalSystem.service.ui.DefaultExternalSystemUiAware;
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager;
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import icons.BldIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.service.project.BldProjectResolver;
import rife.bld.idea.service.task.BldTaskManager;
import rife.bld.idea.settings.*;
import rife.bld.idea.utils.BldConstants;

import javax.swing.*;
import java.io.File;
import java.util.List;

public final class BldManager
    implements ExternalSystemConfigurableAware, ExternalSystemUiAware, ExternalSystemAutoImportAware, StartupActivity, ExternalSystemManager<
    BldProjectSettings,
    BldSettingsListener,
    BldSettings,
    BldLocalSettings,
    BldExecutionSettings> {
    private static final Logger LOG = Logger.getInstance(BldManager.class);

    @NotNull
    private final ExternalSystemAutoImportAware autoImportDelegate_ = new CachingExternalSystemAutoImportAware(new BldAutoImportAware());

    @NotNull @Override public ProjectSystemId getSystemId() {
        return BldConstants.SYSTEM_ID;
    }
    
    @NotNull @Override public Function<Project, BldSettings> getSettingsProvider() {
        return BldSettings::getInstance;
    }

    @NotNull @Override public Function<Project, BldLocalSettings> getLocalSettingsProvider() {
        return BldLocalSettings::getInstance;
    }
    @NotNull
    @Override
    public Function<Pair<Project, String>, BldExecutionSettings> getExecutionSettingsProvider() {
        return pair -> {
            var project = pair.first;
            var projectPath = pair.second;
            var result = new BldExecutionSettings();
            return result;
        };
    }

    @Override
    public void enhanceRemoteProcessing(@NotNull SimpleJavaParameters parameters) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Class<? extends ExternalSystemProjectResolver<BldExecutionSettings>> getProjectResolverClass() {
        return BldProjectResolver.class;
    }

    @Override
    public Class<? extends ExternalSystemTaskManager<BldExecutionSettings>> getTaskManagerClass() {
        return BldTaskManager.class;
    }

    @Override
    public @NotNull FileChooserDescriptor getExternalProjectDescriptor() {
        return new FileChooserDescriptor(true, true, false, false, false, false) {
            @Override
            public boolean isFileSelectable(VirtualFile file) {
                return super.isFileSelectable(file);
            }
        };
    }

    @NotNull
    @Override
    public Configurable getConfigurable(@NotNull Project project) {
        return new BldConfigurable(project);
    }

    @Override
    public @NotNull FileChooserDescriptor getExternalProjectConfigDescriptor() {
        return FileChooserDescriptorFactory.createSingleFolderDescriptor();
    }

    @Override
    public @NotNull Icon getProjectIcon() {
        return BldIcons.Icon;
    }

    @Override
    public @NotNull Icon getTaskIcon() {
        return DefaultExternalSystemUiAware.INSTANCE.getTaskIcon();
    }

    @NotNull
    @Override
    public String getProjectRepresentationName(@NotNull String targetProjectPath, @Nullable String rootProjectPath) {
        return ExternalSystemApiUtil.getProjectRepresentationName(targetProjectPath, rootProjectPath);
    }

    @Nullable
    @Override
    public String getAffectedExternalProjectPath(@NotNull String changedFileOrDirPath, @NotNull Project project) {
        return autoImportDelegate_.getAffectedExternalProjectPath(changedFileOrDirPath, project);
    }

    @Override
    public List<File> getAffectedExternalProjectFiles(String projectPath, @NotNull Project project) {
        return autoImportDelegate_.getAffectedExternalProjectFiles(projectPath, project);
    }

    @Override
    public boolean isApplicable(@Nullable ProjectResolverPolicy resolverPolicy) {
        return autoImportDelegate_.isApplicable(resolverPolicy);
    }

    @Override
    public void runActivity(@NotNull Project project) {
    }
}
