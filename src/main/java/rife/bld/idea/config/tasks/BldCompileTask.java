/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.config.tasks;


import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;
import org.jetbrains.annotations.NotNull;

import static com.intellij.ide.macro.CompilerContextMakeMacro.COMPILER_CONTEXT_MAKE_KEY;

abstract class BldCompileTask implements CompileTask {
    @NotNull
    protected static DataContext createDataContext(CompileContext context) {
        var project = context.getProject();
        var scope = context.getCompileScope();
        var modules = ReadAction.compute(scope::getAffectedModules);
        return SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .add(PlatformCoreDataKeys.MODULE, modules.length == 1? modules[0] : null)
            .add(LangDataKeys.MODULE_CONTEXT_ARRAY, modules)
            .add(COMPILER_CONTEXT_MAKE_KEY, context.isMake())
            .build();
    }
}
