package rife.bld.idea.project;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import icons.BldIcons;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.execution.BldExecution;
import rife.bld.idea.utils.BldBundle;

final class BldProjectOfflineAction extends ToggleAction implements DumbAware {
    private final Project project_;

    public BldProjectOfflineAction(Project project) {
        super(BldBundle.messagePointer("offline.bld.command.action.name"),
            BldBundle.messagePointer("offline.bld.command.action.description"), AllIcons.Actions.OfflineMode);
        project_ = project;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        boolean state = !isSelected(e);
        setSelected(e, state);
        Presentation presentation = e.getPresentation();
        Toggleable.setSelected(presentation, state);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return BldExecution.getInstance(project_).isOffline();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        BldExecution.getInstance(project_).setOffline(state);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        final var presentation = event.getPresentation();
        presentation.setText(BldBundle.messagePointer("offline.bld.command.action.name"));
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
