/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.config;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.EventDispatcher;
import com.intellij.util.TimeoutUtil;
import com.intellij.util.concurrency.Semaphore;
import icons.BldIcons;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.console.BldConsoleManager;
import rife.bld.idea.console.BldConsoleWindowFactory;
import rife.bld.idea.events.ExecuteAfterCompilationEvent;
import rife.bld.idea.events.ExecuteBeforeCompilationEvent;
import rife.bld.idea.events.ExecutionEvent;
import rife.bld.idea.execution.BldBuildListener;
import rife.bld.idea.execution.BldDependencyTree;
import rife.bld.idea.execution.BldExecution;
import rife.bld.idea.execution.BldExecutionFlags;
import rife.bld.idea.project.BldProjectWindowFactory;
import rife.bld.idea.project.BldProjectActionExecuteCommand;
import rife.bld.idea.utils.BldBundle;
import rife.bld.idea.utils.BldConstants;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service(Service.Level.PROJECT)
@State(name = "BldConfiguration", storages = @Storage("bld.xml"), useLoadedStateAsExisting = false)
public final class BldConfiguration implements PersistentStateComponent<Element>, Disposable {
    private static final Logger LOG = Logger.getInstance(BldConfiguration.class);
    @NonNls public static final String ACTION_ID_PREFIX = "Bld_";

    @NonNls private static final String ELEMENT_EVENTS = "events";
    @NonNls private static final String ELEMENT_EXECUTE_ON = "executeOn";
    @NonNls private static final String ELEMENT_EVENT = "event";
    @NonNls private static final String ELEMENT_COMMAND = "command";

    private final Project project_;
    private final Map<ExecutionEvent, String> eventCommandMap_ = Collections.synchronizedMap(new HashMap<>());
    private final EventDispatcher<BldConfigurationListener> eventDispatcher_ = EventDispatcher.create(BldConfigurationListener.class);

    private final List<BldBuildCommand> commands_ = new CopyOnWriteArrayList<>();
    private final Map<String, BldBuildCommand> commandsMap_ = Collections.synchronizedMap(new HashMap<>());
    private final BldDependencyTree dependencyTree_ = new BldDependencyTree();

    private static final Comparator<BldBuildCommand> commandComparator = (command1, command2) -> {
        final String name1 = command1.name();
        if (name1 == null) return -1;
        final String name2 = command2.name();
        if (name2 == null) return 1;
        return name1.compareToIgnoreCase(name2);
    };

    private volatile boolean initialized_ = false;

    public BldConfiguration(final Project project) {
        project_ = project;
    }

    public static BldConfiguration instance(final @NotNull Project project) {
        return project.getService(BldConfiguration.class);
    }

    public static String getActionIdPrefix(final @NotNull Project project) {
        return ACTION_ID_PREFIX + project.getLocationHash();
    }

    @Override
    public void dispose() {
        // no-op
    }

    private static class EventElementComparator implements Comparator<Element> {
        static final Comparator<? super Element> INSTANCE = new EventElementComparator();

        private static final String[] COMPARABLE_ATTRIB_NAMES = new String[] {
            ELEMENT_EVENT,
            ELEMENT_COMMAND
        };

        @Override
        public int compare(final Element o1, final Element o2) {
            for (var attrib_name : COMPARABLE_ATTRIB_NAMES) {
                final int values_equal = Comparing.compare(o1.getAttributeValue(attrib_name), o2.getAttributeValue(attrib_name));
                if (values_equal != 0) {
                    return values_equal;
                }
            }
            return 0;
        }
    }

    private void saveEvents(final Element element) {
        List<Element> events = null;
        final Set<String> saved_events = new HashSet<>();
        synchronized (eventCommandMap_) {
            for (final var event : eventCommandMap_.keySet()) {
                final var command = eventCommandMap_.get(event);
                var event_element = new Element(ELEMENT_EXECUTE_ON);
                event_element.setAttribute(ELEMENT_EVENT, event.getTypeId());
                event_element.setAttribute(ELEMENT_COMMAND, command);

                final String id = event.writeExternal(event_element, project_);
                if (saved_events.contains(id)) continue;
                saved_events.add(id);

                if (events == null) {
                    events = new ArrayList<>();
                }
                events.add(event_element);
            }
        }

        if (events != null) {
            events.sort(EventElementComparator.INSTANCE);
            for (var event_element : events) {
                element.addContent(event_element);
            }
        }
    }

    @Override
    public Element getState() {
        final var state = new Element("state");
        ReadAction.run(() -> {
            final var element = new Element(ELEMENT_EVENTS);
            saveEvents(element);
            state.addContent(element);
        });
        return state;
    }

    @Override
    public void loadState(@NotNull Element state) {
        for (var events_element : state.getChildren(ELEMENT_EVENTS)) {
            for (var event_element : events_element.getChildren()) {
                final var event_id = event_element.getAttributeValue(ELEMENT_EVENT);
                final var command_name = event_element.getAttributeValue(ELEMENT_COMMAND);

                ExecutionEvent event = null;
                if (ExecuteBeforeCompilationEvent.TYPE_ID.equals(event_id)) {
                    event = ExecuteBeforeCompilationEvent.instance();
                } else if (ExecuteAfterCompilationEvent.TYPE_ID.equals(event_id)) {
                    event = ExecuteAfterCompilationEvent.instance();
                }

                if (event != null) {
                    try {
                        event.readExternal(event_element, project_);
                        setCommandForEvent(command_name, event);
                    } catch (InvalidDataException readFailed) {
                        LOG.info(readFailed.getMessage());
                    }
                }
            }
        }
    }

    public boolean isInitialized() {
        return initialized_;
    }

    public List<BldBuildCommand> getCommands() {
        return Collections.unmodifiableList(commands_);
    }

    public BldBuildCommand findCommand(String name) {
        return commandsMap_.get(name);
    }

    public BldDependencyTree getDependencyTree() {
        return dependencyTree_;
    }

    public void setDependencyTree(BldDependencyTree tree) {
        dependencyTree_.clear();
        dependencyTree_.addAll(tree);

        ApplicationManager.getApplication().invokeLater(
            () -> eventDispatcher_.getMulticaster().configurationChanged(),
            ModalityState.any()
        );
    }

    public void setupComplete() {
        initialized_ = true;

        ApplicationManager.getApplication().invokeLater(
            () -> {
                ToolWindowManager.getInstance(project_).registerToolWindow(BldConstants.CONSOLE_NAME, (builder) -> {
                    builder.icon = BldIcons.Bld;
                    builder.anchor = ToolWindowAnchor.BOTTOM;
                    builder.contentFactory = new BldConsoleWindowFactory();
                    return null;
                });

                ToolWindowManager.getInstance(project_).registerToolWindow(BldConstants.PROJECT_NAME, (builder) -> {
                    builder.icon = BldIcons.Bld;
                    builder.anchor = ToolWindowAnchor.RIGHT;
                    builder.contentFactory = new BldProjectWindowFactory();
                    return null;
                });

                eventDispatcher_.getMulticaster().configurationLoaded();
            },
            ModalityState.any()
        );
    }

    public void setCommands(List<BldBuildCommand> commands) {
        commands_.clear();

        final var sorted = new ArrayList<>(commands);
        sorted.sort(commandComparator);
        commands_.addAll(sorted);

        commandsMap_.clear();
        sorted.forEach(cmd -> commandsMap_.put(cmd.name(), cmd));

        ReadAction.run(() -> {
            synchronized (this) {
                if (!project_.isDisposed()) {
                    // unregister bld actions
                    var actionManager = ActionManagerEx.getInstanceEx();
                    for (var oldId : actionManager.getActionIdList(getActionIdPrefix(project_))) {
                        actionManager.unregisterAction(oldId);
                    }

                    // register project actions
                    for (var command : sorted) {
                        final var action_id = command.actionId();
                        if (action_id != null) {
                            final var action = new BldProjectActionExecuteCommand(project_, command.name(), command.description());
                            actionManager.registerAction(action_id, action);
                        }
                    }
                }
            }
        });

        ApplicationManager.getApplication().invokeLater(
            () -> eventDispatcher_.getMulticaster().configurationChanged(),
            ModalityState.any()
        );
    }

    public void addBldConfigurationListener(final BldConfigurationListener listener) {
        eventDispatcher_.addListener(listener);
    }

    public void removeBldConfigurationListener(final BldConfigurationListener listener) {
        eventDispatcher_.removeListener(listener);
    }

    public List<ExecutionEvent> getEventsForCommand(final BldBuildCommand command) {
        final var list = new ArrayList<ExecutionEvent>();
        synchronized (eventCommandMap_) {
            for (final var event : eventCommandMap_.keySet()) {
                final var event_command = getCommandForEvent(event);
                if (command.equals(event_command)) {
                    list.add(event);
                }
            }
        }
        return list;
    }

    @Nullable
    public BldBuildCommand getCommandForEvent(final ExecutionEvent event) {
        final var command_name = eventCommandMap_.get(event);
        if (command_name == null) {
            return null;
        }

        return commandsMap_.get(command_name);
    }

    public void setCommandForEvent(final String commandName, final ExecutionEvent event) {
        eventCommandMap_.put(event, commandName);
    }

    public void clearCommandForEvent(final ExecutionEvent event) {
        eventCommandMap_.remove(event);
    }


    public boolean executeCommandBeforeCompile(final CompileContext compileContext, final DataContext dataContext) {
        return runCommandSynchronously(compileContext, dataContext, ExecuteBeforeCompilationEvent.instance());
    }

    public boolean executeCommandAfterCompile(final CompileContext compileContext, final DataContext dataContext) {
        return runCommandSynchronously(compileContext, dataContext, ExecuteAfterCompilationEvent.instance());
    }

    private boolean runCommandSynchronously(CompileContext compileContext, final DataContext dataContext, ExecutionEvent event) {
        if (!isInitialized()) {
            return true;
        }

        ApplicationManager.getApplication().assertIsNonDispatchThread();
        final var progress = compileContext.getProgressIndicator();
        progress.pushState();
        try {
            final var command = getCommandForEvent(event);
            if (command == null) {
                // no task assigned
                return true;
            }

            if (ExecuteAfterCompilationEvent.TYPE_ID.equals(event.getTypeId()) && compileContext.getMessageCount(CompilerMessageCategory.ERROR) > 0) {
                compileContext.addMessage(
                    CompilerMessageCategory.INFORMATION, BldBundle.message("bld.message.skip.command.after.compilation.errors", command.name()), null, -1, -1
                );
                return true;
            }

            progress.setText(BldBundle.message("bld.progress.text.running.commands"));
            return executeCommandSynchronously(dataContext, command);
        }
        finally {
            progress.popState();
        }
    }

    private static boolean executeCommandSynchronously(final DataContext dataContext, final BldBuildCommand command) {
        final var command_done = new Semaphore();
        command_done.down();
        final var result = Ref.create(Boolean.FALSE);

        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                final Project project = dataContext.getData(CommonDataKeys.PROJECT);
                if (project == null || project.isDisposed()) {
                    command_done.up();
                }
                else {
                    var task = new Task.Backgroundable(null, BldBundle.message("bld.build.progress.dialog.title"), true) {
                        public void run(@NotNull ProgressIndicator indicator) {
                            BldConsoleManager.showTaskMessage(BldBundle.message("bld.project.console.commands", command.name()), ConsoleViewContentType.USER_INPUT, project);
                            BldExecution.instance(project).executeCommands(new BldExecutionFlags(), command, state -> {
                                result.set((state == BldBuildListener.FINISHED_SUCCESSFULLY));
                                command_done.up();
                            });
                        }
                    };
                    task.queue();
                }
            }
            catch (Throwable e) {
                command_done.up();
                LOG.error(e);
            }
        });
        command_done.waitFor();
        return result.get();
    }
}
