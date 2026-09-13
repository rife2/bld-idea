/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.testFramework.DumbModeTestUtils;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import rife.bld.idea.execution.BldExecution;

import java.util.List;

public class BldProjectActionEditMainTest extends LightJavaCodeInsightFixtureTestCase {
    public void testOpensTheBuildClassWhileIndexing() {
        var project_dir = ProjectUtil.guessProjectDir(getProject());
        assertNotNull(project_dir);
        myFixture.addFileToProject("bld", "#!/usr/bin/env bash\njava -jar \"$(dirname \"$0\")/lib/bld/bld-wrapper.jar\" \"$0\" --build com.example.MyAppBuild \"$@\"\n");
        var build_file = myFixture.addFileToProject("src/bld/java/com/example/MyAppBuild.java", "package com.example;\npublic class MyAppBuild {}\n").getVirtualFile();
        assertTrue(BldExecution.instance(getProject()).setupProject());

        var action = new BldProjectActionEditMain(getProject());
        var event = AnActionEvent.createFromDataContext("test", null, SimpleDataContext.getProjectContext(getProject()));
        DumbModeTestUtils.runInDumbModeSynchronously(getProject(), () -> action.actionPerformed(event));
        PlatformTestUtil.dispatchAllEventsInIdeEventQueue();

        assertEquals(List.of(build_file), List.of(FileEditorManager.getInstance(getProject()).getOpenFiles()));
    }
}
