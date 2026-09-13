/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.utils.BldBundle;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service(Service.Level.PROJECT)
public final class BldRefresh implements Disposable {
    // a save tends to touch several files, the build is reloaded once they settle
    private static final long SETTLE_DELAY_MS = 1000;

    private final Project project_;
    private boolean running_ = false;
    private boolean pending_ = false;
    private String sdk_ = null;
    ScheduledFuture<?> scheduled_ = null;

    public BldRefresh(@NotNull Project project) {
        project_ = project;
    }

    public static BldRefresh instance(@NotNull Project project) {
        return project.getService(BldRefresh.class);
    }

    public void watch(@NotNull VirtualFile projectDir) {
        synchronized (this) {
            sdk_ = projectSdk();
        }

        var build_sources = projectDir.getPath() + "/src/bld/";
        var wrapper_properties = projectDir.getPath() + "/lib/bld/bld-wrapper.properties";
        var connection = project_.getMessageBus().connect(this);
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                for (var event : events) {
                    var path = event.getPath();
                    if (path.startsWith(build_sources) || path.equals(wrapper_properties)) {
                        refreshSoon();
                        return;
                    }
                }
            }
        });
        // bld runs on the project SDK, which a freshly opened project can get only after it started:
        // the SDK it names is registered then, which isn't a roots change of the project
        connection.subscribe(ModuleRootListener.TOPIC, new ModuleRootListener() {
            @Override
            public void rootsChanged(@NotNull ModuleRootEvent event) {
                refreshIfProjectSdkChanged();
            }
        });
        connection.subscribe(ProjectJdkTable.JDK_TABLE_TOPIC, new ProjectJdkTable.Listener() {
            @Override
            public void jdkAdded(@NotNull Sdk jdk) {
                refreshIfProjectSdkChanged();
            }

            @Override
            public void jdkRemoved(@NotNull Sdk jdk) {
                refreshIfProjectSdkChanged();
            }

            @Override
            public void jdkNameChanged(@NotNull Sdk jdk, @NotNull String previousName) {
                refreshIfProjectSdkChanged();
            }
        });
    }

    private void refreshIfProjectSdkChanged() {
        boolean changed;
        synchronized (this) {
            changed = !Objects.equals(sdk_, projectSdk());
        }
        if (changed) {
            refreshSoon();
        }
    }

    private String projectSdk() {
        var sdk = ProjectRootManager.getInstance(project_).getProjectSdk();
        return sdk == null ? null : sdk.getName() + '|' + sdk.getHomePath();
    }

    public synchronized void refreshSoon() {
        if (scheduled_ != null) {
            scheduled_.cancel(false);
        }
        scheduled_ = AppExecutorUtil.getAppScheduledExecutorService().schedule(this::refresh, SETTLE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    public void refresh() {
        synchronized (this) {
            if (project_.isDisposed()) {
                return;
            }
            // a change during a refresh may not be in what it read, so it runs again after
            if (running_) {
                pending_ = true;
                return;
            }
            running_ = true;
            sdk_ = projectSdk();
        }

        new Task.Backgroundable(project_, BldBundle.message("bld.progress.text.loading.config"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    var execution = BldExecution.instance(project_);
                    BldExecuteListCommands.run(execution);
                    BldExecuteDependencyTree.run(execution);
                } finally {
                    finished();
                }
            }
        }.queue();
    }

    private void finished() {
        boolean again;
        synchronized (this) {
            running_ = false;
            again = pending_;
            pending_ = false;
        }
        if (again) {
            refresh();
        }
    }

    @Override
    public synchronized void dispose() {
        if (scheduled_ != null) {
            scheduled_.cancel(false);
        }
    }
}
