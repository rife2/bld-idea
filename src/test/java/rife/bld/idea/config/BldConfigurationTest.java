/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.config;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class BldConfigurationTest extends BasePlatformTestCase {
    public void testShowingTheConsoleIsAWorkspacePreference() {
        var configuration = BldConfiguration.instance(getProject());
        var properties = PropertiesComponent.getInstance(getProject());
        try {
            assertFalse(configuration.isActivateConsoleOnExecute());

            configuration.setActivateConsoleOnExecute(true);
            assertTrue(configuration.isActivateConsoleOnExecute());
            assertTrue(properties.isValueSet("bld.activateConsoleOnExecute"));
            assertFalse(JDOMUtil.write(configuration.getState()).contains("activateConsole"));

            configuration.setActivateConsoleOnExecute(false);
            assertFalse(configuration.isActivateConsoleOnExecute());
            assertFalse(properties.isValueSet("bld.activateConsoleOnExecute"));
        } finally {
            configuration.setActivateConsoleOnExecute(false);
        }
    }
}
