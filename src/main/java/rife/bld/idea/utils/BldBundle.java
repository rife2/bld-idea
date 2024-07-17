/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.utils;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public class BldBundle {
    private static final @NonNls String BUNDLE = "messages.BldBundle";
    private static final DynamicBundle INSTANCE = new DynamicBundle(BldBundle.class, BUNDLE);

    private BldBundle() {}

    public static @NotNull @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object ... params) {
        return INSTANCE.getMessage(key, params);
    }

    public static @NotNull Supplier<String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object ... params) {
        return INSTANCE.getLazyMessage(key, params);
    }
}
