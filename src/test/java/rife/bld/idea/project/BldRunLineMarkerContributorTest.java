/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

public class BldRunLineMarkerContributorTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.addClass("""
            package rife.bld;
            import java.lang.annotation.*;
            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            public @interface BuildCommand {
                String value() default "";
                String summary() default "";
            }
            """);
        myFixture.addClass("""
            package rife.bld;
            public class Project {
                @BuildCommand(summary = "Compiles the project")
                public void compile() {}
                @BuildCommand(value = "dependency-tree", summary = "Outputs the dependency tree")
                public void dependencyTree() {}
            }
            """);
    }

    public void testCommandNames() {
        var file = (PsiJavaFile) myFixture.configureByText("MyAppBuild.java", """
            package com.example;
            import rife.bld.BuildCommand;
            import rife.bld.Project;
            public class MyAppBuild extends Project {
                @BuildCommand(summary = "Says Hello")
                public void hello() {}
                @BuildCommand(value = "say-bye")
                public void bye() {}
                @Override
                public void compile() {}
                @Override
                public void dependencyTree() {}
                public void helper() {}
            }
            """);
        var build_class = file.getClasses()[0];

        assertEquals("hello", commandName(build_class.findMethodsByName("hello", false)[0]));
        assertEquals("say-bye", commandName(build_class.findMethodsByName("bye", false)[0]));
        assertEquals("compile", commandName(build_class.findMethodsByName("compile", false)[0]));
        assertEquals("dependency-tree", commandName(build_class.findMethodsByName("dependencyTree", false)[0]));
        assertNull(commandName(build_class.findMethodsByName("helper", false)[0]));
    }

    private static String commandName(PsiMethod method) {
        return BldRunLineMarkerContributor.commandName(method);
    }
}
