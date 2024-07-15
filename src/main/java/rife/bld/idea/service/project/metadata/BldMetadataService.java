package rife.bld.idea.service.project.metadata;

import com.intellij.openapi.externalSystem.ExternalSystemModulePropertyManager;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.Key;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.externalSystem.service.project.manage.AbstractModuleDataService;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.utils.BldConstants;

import java.util.Collection;

public class BldMetadataService extends AbstractModuleDataService<TargetMetadata> {
    @NotNull
    @Override
    public Key<TargetMetadata> getTargetDataKey() {
        return TargetMetadata.KEY;
    }

    @Override
    public void importData(@NotNull Collection<? extends DataNode<TargetMetadata>> toImport,
                           @Nullable ProjectData projectData,
                           @NotNull Project project,
                           @NotNull IdeModifiableModelsProvider modelsProvider) {
        // TODO
//        BldSettings.getInstance(project).setResolverVersion(BldResolver.VERSION);
        super.importData(toImport, projectData, project, modelsProvider);
    }

    @Override
    protected void setModuleOptions(Module module, DataNode<TargetMetadata> moduleDataNode) {
        super.setModuleOptions(module, moduleDataNode);
        module.getService(ModuleTargetMetadataStorage.class).loadState(new ModuleTargetMetadataStorage.State(moduleDataNode.getData()));
        ExternalSystemModulePropertyManager.getInstance(module).setExternalModuleType(BldConstants.BLD_TARGET_MODULE_TYPE);
    }
}
