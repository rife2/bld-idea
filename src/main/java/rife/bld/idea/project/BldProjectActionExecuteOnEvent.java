package rife.bld.idea.project;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.tree.StructureTreeModel;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.config.BldBuildCommand;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.events.ExecutionEvent;

final class BldProjectActionExecuteOnEvent extends ToggleAction {
    private final Project project_;
    private final StructureTreeModel treeModel_;
    private final BldBuildCommand command_;
    private final ExecutionEvent event_;

    BldProjectActionExecuteOnEvent(final Project project, final StructureTreeModel treeModel, final BldBuildCommand command, final ExecutionEvent executionEvent) {
        super(executionEvent.getPresentableName());
        project_ = project;
        treeModel_ = treeModel;
        command_ = command;
        event_ = executionEvent;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return command_.equals(BldConfiguration.instance(project_).getCommandForEvent(event_));
    }

    @Override
    public void setSelected(@NotNull AnActionEvent event, boolean state) {
        final var config = BldConfiguration.instance(project_);
        if (state) {
            config.setCommandForEvent(command_.name(), event_);
        } else {
            config.clearCommandForEvent(event_);
        }
        treeModel_.invalidateAsync();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
