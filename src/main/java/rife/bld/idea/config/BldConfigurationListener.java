/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.config;

import java.util.EventListener;

public interface BldConfigurationListener extends EventListener {
    default void configurationLoaded() {
    }

    default void configurationChanged() {
    }
}
