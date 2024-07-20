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

final class BldProjectEditMainAction extends AnAction implements DumbAware {
    private final Project project_;

    public BldProjectEditMainAction(Project project) {
        super(BldBundle.messagePointer("bld.action.edit.name"),
            BldBundle.messagePointer("bld.action.edit.description"), AllIcons.Actions.Edit);

        project_ = project;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var main_class = BldExecution.getInstance(project_).getBldMainClass();
        var psi_class = JavaPsiFacade.getInstance(project_).findClass(main_class, GlobalSearchScope.allScope(project_));
        if (psi_class != null) {
            FileEditorManager.getInstance(project_).openFile(psi_class.getContainingFile().getVirtualFile());
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        final var presentation = event.getPresentation();
        presentation.setText(BldBundle.messagePointer("bld.action.edit.name"));
        presentation.setEnabled(true);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
