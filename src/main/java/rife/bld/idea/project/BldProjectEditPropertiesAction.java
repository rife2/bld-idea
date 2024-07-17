package rife.bld.idea.project;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.execution.BldExecution;
import rife.bld.idea.utils.BldBundle;

final class BldProjectEditPropertiesAction extends AnAction implements DumbAware {
    private final Project project_;

    public BldProjectEditPropertiesAction(Project project) {
        super(BldBundle.messagePointer("properties.bld.command.action.name"),
            BldBundle.messagePointer("properties.bld.command.action.description"), AllIcons.Actions.EditScheme);

        project_ = project;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var properties = BldExecution.getInstance(project_).getBldProperties();
        if (properties != null) {
            FileEditorManager.getInstance(project_).openFile(properties);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        final var presentation = event.getPresentation();
        presentation.setText(BldBundle.messagePointer("properties.bld.command.action.name"));
        presentation.setEnabled(BldExecution.getInstance(project_).hasBldProperties());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
