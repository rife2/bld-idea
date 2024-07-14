/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.listeners;

import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.IdeFrame;
import org.jetbrains.annotations.NotNull;

public class BldApplicationActivationListener implements ApplicationActivationListener {
    @Override
    public void applicationActivated(@NotNull IdeFrame ideFrame) {
        Logger.getInstance(BldApplicationActivationListener.class).info("bld application activated");
    }
}
