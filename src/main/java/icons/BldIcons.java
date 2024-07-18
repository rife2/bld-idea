/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class BldIcons {
    private static javax.swing.Icon load(String path) {
        return IconLoader.getIcon(path, BldIcons.class);
    }

    public static final Icon Bld = load("/icons/bldIcon.svg");
    public static final Icon Online = load("/icons/online.svg");
    public static final Icon Offline = load("/icons/offline.svg");
}
