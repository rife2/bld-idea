/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

public class BldRunConfigurationSettings implements JDOMExternalizable {
    private static final String SETTINGS = "settings";
    private static final String PROPERTY = "property";
    private static final String COMMAND = "command";

    String commandName_ = null;
    final List<BldRunProperty> properties_ = new ArrayList<>();

    public BldRunConfigurationSettings() {
    }

    public BldRunConfigurationSettings(String commandName) {
        commandName_ = commandName;
    }

    @Override
    public String toString() {
        return commandName_;
    }

    public BldRunConfigurationSettings copy() {
        final BldRunConfigurationSettings copy = new BldRunConfigurationSettings(commandName_);
        BldRunConfiguration.copyProperties(properties_, copy.properties_);
        return copy;
    }

    @Override
    public void readExternal(Element element)
    throws InvalidDataException {
        element = element.getChild(SETTINGS);
        if (element != null) {
            commandName_ = element.getAttributeValue(COMMAND);
            properties_.clear();
            for (Element pe : element.getChildren(PROPERTY)) {
                BldRunProperty prop = new BldRunProperty();
                prop.readExternal(pe);
                properties_.add(prop);
            }
        }
    }

    @Override
    public void writeExternal(Element element)
    throws WriteExternalException {
        if (commandName_ != null) {
            final Element settingsElem = new Element(SETTINGS);
            settingsElem.setAttribute(COMMAND, commandName_);
            for (var property : properties_) {
                final var pe = new Element(PROPERTY);
                property.writeExternal(pe);
                settingsElem.addContent(pe);
            }
            element.addContent(settingsElem);
        }
    }
}
