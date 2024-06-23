package rife.bld.idea.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class BldToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        var window = new BldToolWindow(toolWindow);
        var content = ContentFactory.getInstance().createContent(window.getContent(), null, false);
        toolWindow.getContentManager().addContent(content);
    }

    static class BldToolWindow {
        public BldToolWindow(ToolWindow toolWindow) {
        }

        public JBPanel<?> getContent() {
            var panel = new JBPanel<>(new BorderLayout());
            panel.add(new JBLabel("This is the bld tool window"));
            return panel;
        }
    }
}
