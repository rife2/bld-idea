/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.utils;

import java.util.regex.Pattern;

public class BldConstants {
    public static final String BLD = "bld";
    public static final String PROJECT_NAME = "bld";
    public static final String CONSOLE_NAME = "bld console";
    public static final String BLD_CONSOLE_TOOLBAR = "BldConsoleToolbar";
    public static final String BLD_EXPLORER_TOOLBAR = "BldExplorerToolbar";
    public static final String WRAPPER_BUILD_ARGUMENT = "--build";
    public static final String WRAPPER_JSON_ARGUMENT = "--json";
    public static final Pattern BUILD_MAIN_CLASS = Pattern.compile(WRAPPER_BUILD_ARGUMENT + "\\s(\\S+)");
}
