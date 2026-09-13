/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.project;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.InheritanceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.execution.BldExecution;
import rife.bld.idea.utils.BldBundle;

public final class BldRunLineMarkerContributor extends RunLineMarkerContributor {
    private static final String BUILD_COMMAND = "rife.bld.BuildCommand";

    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        if (!(element instanceof PsiIdentifier) ||
            !(element.getParent() instanceof PsiMethod method) ||
            method.getNameIdentifier() != element ||
            method.hasParameters()) {
            return null;
        }

        var project = element.getProject();
        var main_class = BldExecution.instance(project).getBldMainClass();
        var containing_class = method.getContainingClass();
        var file = element.getContainingFile().getVirtualFile();
        if (main_class == null || containing_class == null || file == null ||
            !ProjectFileIndex.getInstance(project).isInContent(file) ||
            !isBuildClass(containing_class, main_class)) {
            return null;
        }

        var command = commandName(method);
        if (command == null) {
            return null;
        }

        var text = BldBundle.message("bld.gutter.run.command", command);
        var action = new BldProjectActionExecuteCommand(project, command, null, text);
        return new Info(AllIcons.RunConfigurations.TestState.Run, new AnAction[]{action}, __ -> text);
    }

    private static boolean isBuildClass(PsiClass psiClass, String mainClass) {
        if (mainClass.equals(psiClass.getQualifiedName())) {
            return true;
        }
        var project = psiClass.getProject();
        var main = JavaPsiFacade.getInstance(project).findClass(mainClass, GlobalSearchScope.projectScope(project));
        return main != null && InheritanceUtil.isInheritorOrSelf(main, psiClass, true);
    }

    // bld names a command after its annotation, walking up to where an overridden command was annotated
    static @Nullable String commandName(PsiMethod method) {
        var annotation = method.getAnnotation(BUILD_COMMAND);
        if (annotation != null) {
            var value = AnnotationUtil.getStringAttributeValue(annotation, "value");
            return value == null || value.isEmpty() ? method.getName() : value;
        }
        for (var super_method : method.findSuperMethods()) {
            var name = commandName(super_method);
            if (name != null) {
                return name;
            }
        }
        return null;
    }
}
