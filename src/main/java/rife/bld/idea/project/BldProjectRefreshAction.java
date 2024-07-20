package rife.bld.idea.project;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.console.BldConsoleManager;
import rife.bld.idea.execution.BldExecution;
import rife.bld.idea.utils.BldBundle;

final class BldProjectRefreshAction extends AnAction implements DumbAware {
    private final Project project_;

    public BldProjectRefreshAction(Project project) {
        super(BldBundle.messagePointer("bld.action.refresh.name"),
            BldBundle.messagePointer("bld.action.refresh.description"), AllIcons.Actions.Refresh);
        project_ = project;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        FileDocumentManager.getInstance().saveAllDocuments();

        new Task.Backgroundable(project_, BldBundle.message("bld.project.progress.refresh"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                BldConsoleManager.showTaskMessage(BldBundle.message("bld.project.console.refresh"), ConsoleViewContentType.USER_INPUT, project_);

                BldExecution.getInstance(project_).listTasks();
            }
        }.queue();
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        final var presentation = event.getPresentation();
        presentation.setText(BldBundle.messagePointer("bld.action.refresh.name"));
        presentation.setEnabled(true);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
