package rife.bld.idea.project;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import icons.BldIcons;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.execution.BldExecution;
import rife.bld.idea.utils.BldBundle;

final class BldProjectOfflineAction extends AnAction implements DumbAware {
    private final Project project_;

    public BldProjectOfflineAction(Project project) {
        super(BldBundle.messagePointer("offline.bld.command.action.name"),
            BldBundle.messagePointer("offline.bld.command.action.description"), BldIcons.Online);
        project_ = project;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var execution = BldExecution.getInstance(project_);
        execution.setOffline(!execution.isOffline());
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        final var presentation = event.getPresentation();
        presentation.setText(BldBundle.messagePointer("offline.bld.command.action.name"));
        if (BldExecution.getInstance(project_).isOffline()) {
            presentation.setIcon(BldIcons.Offline);
        }
        else {
            presentation.setIcon(BldIcons.Online);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
