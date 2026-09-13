/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.openapi.project.RootsChangeRescanningInfo;
import org.jetbrains.annotations.NotNull;

public class BldRefreshTest extends LightJavaCodeInsightFixtureTestCase {
    // a project opened for the first time names an SDK that the IDE only sets up after the project started
    public void testRefreshesOnceTheNamedProjectSdkIsSetUp() throws Exception {
        var root_manager = ProjectRootManager.getInstance(getProject());
        var previous_sdk = root_manager.getProjectSdk();
        var refresh = BldRefresh.instance(getProject());
        var sdk = IdeaTestUtil.createMockJdk("bld refresh test jdk", IdeaTestUtil.getMockJdk17Path().getPath());
        var other_sdk = IdeaTestUtil.getMockJdk21();
        try {
            WriteAction.runAndWait(() -> root_manager.setProjectSdk(sdk));
            assertNull("the named SDK isn't set up yet", root_manager.getProjectSdk());

            refresh.watch(myFixture.getTempDirFixture().findOrCreateDir("project"));

            // other changes don't need a refresh, like the jars bld downloads or another SDK
            var roots_changes = new int[1];
            getProject().getMessageBus().connect(getTestRootDisposable()).subscribe(ModuleRootListener.TOPIC, new ModuleRootListener() {
                @Override
                public void rootsChanged(@NotNull ModuleRootEvent event) {
                    roots_changes[0]++;
                }
            });
            // without rescanning, adding a real library starts indexing that races the fixture's teardown
            WriteAction.runAndWait(() -> ProjectRootManagerEx.getInstanceEx(getProject()).makeRootsChange(EmptyRunnable.getInstance(), RootsChangeRescanningInfo.NO_RESCAN_NEEDED));
            assertEquals(1, roots_changes[0]);
            WriteAction.runAndWait(() -> ProjectJdkTable.getInstance().addJdk(other_sdk));
            assertNull(refresh.scheduled_);

            WriteAction.runAndWait(() -> ProjectJdkTable.getInstance().addJdk(sdk));
            assertNotNull(root_manager.getProjectSdk());
            assertNotNull(refresh.scheduled_);
        } finally {
            if (refresh.scheduled_ != null) {
                refresh.scheduled_.cancel(false);
            }
            WriteAction.runAndWait(() -> {
                root_manager.setProjectSdk(previous_sdk);
                var table = ProjectJdkTable.getInstance();
                if (table.findJdk(sdk.getName()) != null) table.removeJdk(sdk);
                if (table.findJdk(other_sdk.getName()) != null) table.removeJdk(other_sdk);
            });
        }
    }
}
