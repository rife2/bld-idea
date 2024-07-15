/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.utils;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import rife.bld.wrapper.Wrapper;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;

import java.util.regex.Pattern;

public class BldConstants {
    @NotNull @NonNls public static final ProjectSystemId SYSTEM_ID = new ProjectSystemId("BLD");

    public static final String BLD = "bld";
    public static final String CONSOLE_NAME = "Bld Console";
    public static final Pattern BUILD_MAIN_CLASS = Pattern.compile(Wrapper.BUILD_ARGUMENT + "\\s(\\S+)");
    public static final String BLD_TARGET_MODULE_TYPE =  "bld.module";

}
