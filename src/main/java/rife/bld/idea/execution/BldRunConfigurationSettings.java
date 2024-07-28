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
    private static final String SETTINGS = "bld.settings";
    private static final String OPTION = "option";
    private static final String PROPERTY = "property";
    private static final String COMMAND = "command";

    String commandName_ = null;
    final List<BldRunOption> options_ = new ArrayList<>();
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
        BldRunConfiguration.copyOptions(options_, copy.options_);
        BldRunConfiguration.copyProperties(properties_, copy.properties_);
        return copy;
    }

    @Override
    public void readExternal(Element element)
    throws InvalidDataException {
        element = element.getChild(SETTINGS);
        if (element != null) {
            commandName_ = element.getAttributeValue(COMMAND);

            options_.clear();
            for (var oe : element.getChildren(OPTION)) {
                var option = new BldRunOption();
                option.readExternal(oe);
                options_.add(option);
            }

            properties_.clear();
            for (var pe : element.getChildren(PROPERTY)) {
                var property = new BldRunProperty();
                property.readExternal(pe);
                properties_.add(property);
            }
        }
    }

    @Override
    public void writeExternal(Element element)
    throws WriteExternalException {
        if (commandName_ != null) {
            final var settings_element = new Element(SETTINGS);
            settings_element.setAttribute(COMMAND, commandName_);

            for (var option : options_) {
                final var oe = new Element(OPTION);
                option.writeExternal(oe);
                settings_element.addContent(oe);
            }

            for (var property : properties_) {
                final var pe = new Element(PROPERTY);
                property.writeExternal(pe);
                settings_element.addContent(pe);
            }

            element.addContent(settings_element);
        }
    }
}
