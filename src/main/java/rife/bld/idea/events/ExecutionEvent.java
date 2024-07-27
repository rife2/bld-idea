/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.events;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;

public abstract class ExecutionEvent {
    public abstract @NonNls String getTypeId();

    public abstract @Nls String getPresentableName();

    public void readExternal(Element element, Project project) throws InvalidDataException {
    }

    public String writeExternal(Element element, Project project) {
        return getTypeId();
    }
}
