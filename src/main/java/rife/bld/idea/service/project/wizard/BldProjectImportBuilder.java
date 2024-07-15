package rife.bld.idea.service.project.wizard;


import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.model.project.ProjectSdkData;
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager;
import com.intellij.openapi.externalSystem.service.project.wizard.AbstractExternalProjectImportBuilder;
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.projectImport.ProjectImportBuilder;
import icons.BldIcons;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.service.settings.ImportFromBldControl;
import rife.bld.idea.utils.BldBundle;
import rife.bld.idea.utils.BldConstants;

import javax.swing.*;
import java.io.File;
import java.util.Optional;

public class BldProjectImportBuilder extends AbstractExternalProjectImportBuilder<ImportFromBldControl> {

    public static BldProjectImportBuilder getInstance(){
        return ProjectImportBuilder.EXTENSIONS_POINT_NAME.findExtensionOrFail(BldProjectImportBuilder.class);
    }

    public BldProjectImportBuilder() {
        super(ProjectDataManager.getInstance(), ImportFromBldControl::new, BldConstants.SYSTEM_ID);
    }

    @NotNull
    @Override
    public String getName() {
        return BldBundle.message("bld.name");
    }

    @Override
    public Icon getIcon() {
        return BldIcons.Icon;
    }

    @Override
    protected void doPrepare(@NotNull WizardContext context) {
        String pathToUse = context.getProjectFileDirectory();
        getControl(context.getProject()).setLinkedProjectPath(pathToUse);
    }

    @Override
    protected void beforeCommit(@NotNull DataNode<ProjectData> dataNode, @NotNull Project project) {

    }

    @NotNull
    @Override
    protected File getExternalProjectConfigToUse(@NotNull File file) {
        return file;
    }

    @Override
    protected void applyExtraSettings(@NotNull WizardContext context) {
        Optional.ofNullable(getExternalProjectNode())
            .map(node -> ExternalSystemApiUtil.find(node, ProjectSdkData.KEY))
            .map(DataNode::getData)
            .map(ProjectSdkData::getSdkName)
            .map(ProjectJdkTable.getInstance()::findJdk)
            .ifPresent(context::setProjectJdk);
    }
}
