package rife.bld.idea.settings;

import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener;
import com.intellij.util.messages.Topic;

public interface BldSettingsListener extends ExternalSystemSettingsListener<BldProjectSettings> {

    @Topic.ProjectLevel
    Topic<BldSettingsListener> TOPIC = new Topic<>(BldSettingsListener.class, Topic.BroadcastDirection.NONE);
}