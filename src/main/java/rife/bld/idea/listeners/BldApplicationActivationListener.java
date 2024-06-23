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
