package rife.bld.idea.toolWindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import rife.bld.wrapper.Wrapper;
import rife.tools.ExceptionUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

public class BldToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        var window = new BldToolWindow(toolWindow);
        var content = ContentFactory.getInstance().createContent(window.getContent(), null, false);
        toolWindow.getContentManager().addContent(content);
    }

    static class BldToolWindow {
        public BldToolWindow(ToolWindow toolWindow) {
            var project = toolWindow.getProject();
            var project_dir = ProjectUtil.guessProjectDir(project);
            if (project_dir == null) {
                Logger.getInstance(BldToolWindowFactory.class).warn("Could not find project directory");
            }
            else {
                var bldMainClass = guessBldMainClass(project_dir);
                if (bldMainClass == null) {
                    Logger.getInstance(BldToolWindowFactory.class).warn("Unable to determine bld's main class for project");
                }
                else {
                    var java_args = new ArrayList<String>();
                    java_args.add("java");
                    java_args.add("-jar");
                    java_args.add(project_dir.getCanonicalPath() + "/lib/bld/bld-wrapper.jar");
                    java_args.add(project_dir.getCanonicalPath() + "/bld");
                    java_args.add(Wrapper.BUILD_ARGUMENT);
                    java_args.add(bldMainClass);

                    var process_builder = new ProcessBuilder(java_args);
                    process_builder.directory(new File(Objects.requireNonNull(project_dir.getCanonicalPath())));
                    process_builder.inheritIO();

                    try {
                        var process = process_builder.start();
                        process.waitFor();
                    } catch (IOException | InterruptedException e) {
                        Logger.getInstance(BldToolWindowFactory.class).warn("Unable to launch bld wrapper: " + ExceptionUtils.getExceptionStackTrace(e));
                    }
                }
            }
        }

        private final static Pattern BUILD_MAIN_CLASS = Pattern.compile(Wrapper.BUILD_ARGUMENT + "\\s(\\S+)");;

        public String guessBldMainClass(VirtualFile projectDir) {
            if (projectDir != null) {
                var project_bld = projectDir.findChild("bld");
                if (project_bld == null) {
                    project_bld = projectDir.findChild("bld.bat");
                }
                if (project_bld != null) {
                    try {
                        var contents = new String(project_bld.contentsToByteArray());
                        var matcher = BUILD_MAIN_CLASS.matcher(contents);
                        if (matcher.find()) return matcher.group(1);
                    } catch (IOException e) {
                        Logger.getInstance(BldToolWindowFactory.class).warn("Unable to read contents of " + project_bld);
                    }
                }
            }
            return null;
        }

        public JBPanel<?> getContent() {
            var panel = new JBPanel<>(new BorderLayout());
            panel.add(new JBLabel("This is the bld tool window"));
            return panel;
        }
    }
}
