/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;


import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.config.BldBuildCommand;
import rife.bld.idea.config.BldConfiguration;
import rife.bld.idea.utils.BldBundle;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BldRunConfiguration extends LocatableConfigurationBase<BldRunConfiguration> implements RunProfileWithCompileBeforeLaunchOption {
    private BldSettings settings_ = new BldSettings();

    public BldRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory) {
        super(project, factory);
    }

    @Override
    public RunConfiguration clone() {
        final BldRunConfiguration configuration = (BldRunConfiguration) super.clone();
        configuration.settings_ = settings_.copy();
        return configuration;
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new BldConfigurationSettingsEditor();
    }

    @Override
    public void checkConfiguration()
    throws RuntimeConfigurationException {
        if (!BldConfiguration.instance(getProject()).isInitialized()) {
            throw new RuntimeConfigurationException(BldBundle.message("bld.dialog.message.config.not.initialized"));
        }
        if (getCommand() == null)
            throw new RuntimeConfigurationException(BldBundle.message("bld.dialog.message.command.not.specified"),
                BldBundle.message("bld.dialog.title.config.missing.parameters"));
    }

    @Override
    public String suggestedName() {
        var command = getCommand();
        return command == null ? null : command.displayName();
    }

    @NotNull
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) {
        return new BldRunProfileState(env);
    }

    @Override
    public void readExternal(@NotNull Element element)
    throws InvalidDataException {
        super.readExternal(element);
        settings_.readExternal(element);
    }

    @Override
    public void writeExternal(@NotNull Element element)
    throws WriteExternalException {
        super.writeExternal(element);
        settings_.writeExternal(element);
    }

    @Nullable
    public BldBuildCommand getCommand() {
        return BldConfiguration.instance(getProject()).findCommand(settings_.commandName_);
    }

    @NotNull
    public List<BldRunProperty> getProperties() {
        return Collections.unmodifiableList(settings_.properties_);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean acceptSettings(BldBuildCommand command) {
        settings_.commandName_ = command.name();
        return true;
    }

    public static class BldSettings implements JDOMExternalizable {
        private static final String SETTINGS = "settings";
        private static final String PROPERTY = "property";
        private static final String COMMAND = "command";
        private String commandName_ = null;
        private final List<BldRunProperty> properties_ = new ArrayList<>();

        public BldSettings() {
        }

        public BldSettings(String commandName) {
            commandName_ = commandName;
        }

        @Override
        public String toString() {
            return commandName_;
        }

        public BldSettings copy() {
            final BldSettings copy = new BldSettings(commandName_);
            copyProperties(properties_, copy.properties_);
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

    private class BldConfigurationSettingsEditor extends SettingsEditor<RunConfiguration> {
        private String commandName_ = null;
        private ExtendableTextField textField_;
        private final PropertiesTable propTable_ = new PropertiesTable();

        private final Runnable myAction = () -> {
            var command = getCommand();
            final BldCommandChooserDialog dlg = new BldCommandChooserDialog(getProject(), command);
            if (dlg.showAndGet()) {
                commandName_ = null;
                command = dlg.getSelectedCommand();
                if (command != null) {
                    commandName_ = command.name();
                }
                updateUI();
            }
        };

        private void updateUI() {
            textField_.setText("");
            if (commandName_ != null) {
                textField_.setText(commandName_);
            }
            propTable_.refreshValues();
            fireEditorStateChanged();
        }

        @Override
        protected void resetEditorFrom(@NotNull RunConfiguration s) {
            final var config = (BldRunConfiguration) s;
            commandName_ = config.settings_.commandName_;
            propTable_.setValues(config.settings_.properties_);
            updateUI();
        }

        @Override
        protected void applyEditorTo(@NotNull RunConfiguration s) {
            final var config = (BldRunConfiguration) s;
            config.settings_.commandName_ = commandName_;
            copyProperties(ContainerUtil.filter(propTable_.getElements(), property -> !propTable_.isEmpty(property)), config.settings_.properties_);
        }

        @NotNull
        @Override
        protected JComponent createEditor() {
            textField_ = new ExtendableTextField().addBrowseExtension(myAction, this);

            final var panel = new JPanel(new BorderLayout());
            panel.add(LabeledComponent.create(textField_, BldBundle.message("bld.label.run.config.command.name"), BorderLayout.WEST), BorderLayout.NORTH);

            final var properties_table_name = BldBundle.message("bld.label.table.name.properties");
            final var table_component = LabeledComponent.create(propTable_.getComponent(), properties_table_name);
            table_component.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            panel.add(table_component, BorderLayout.CENTER);
            return panel;
        }
    }

    private static class PropertiesTable extends ListTableWithButtons<BldRunProperty> {
        @Override
        protected ListTableModel<BldRunProperty> createListModel() {
            final ColumnInfo<BldRunProperty, @NlsContexts.ListItem String> nameColumn = new TableColumn(BldBundle.message("bld.column.name.config.property.name")) {
                @Nullable
                @Override
                public String valueOf(BldRunProperty property) {
                    return property.getPropertyName();
                }

                @Override
                public void setValue(BldRunProperty property, String value) {
                    property.setPropertyName(value);
                }
            };
            final ColumnInfo<BldRunProperty, @NlsContexts.ListItem String> valueColumn = new TableColumn(BldBundle.message("bld.column.name.config.property.value")) {
                @Nullable
                @Override
                public String valueOf(BldRunProperty property) {
                    return property.getPropertyValue();
                }

                @Override
                public void setValue(BldRunProperty property, String value) {
                    property.setPropertyValue(value);
                }
            };
            return new ListTableModel<>(nameColumn, valueColumn);
        }

        @Override
        protected BldRunProperty createElement() {
            return new BldRunProperty();
        }

        @Override
        protected boolean isEmpty(BldRunProperty element) {
            return StringUtil.isEmpty(element.getPropertyName()) && StringUtil.isEmpty(element.getPropertyValue());
        }

        @Override
        protected BldRunProperty cloneElement(BldRunProperty p) {
            return p.clone();
        }

        @Override
        protected boolean canDeleteElement(BldRunProperty selection) {
            return true;
        }

        @Override
        public List<BldRunProperty> getElements() {
            return super.getElements();
        }

        private abstract static class TableColumn extends ElementsColumnInfoBase<BldRunProperty> {
            TableColumn(final @NlsContexts.ColumnName String name) {
                super(name);
            }

            @Override
            public boolean isCellEditable(BldRunProperty property) {
                return true;
            }

            @Nullable
            @Override
            protected String getDescription(BldRunProperty element) {
                return null;
            }
        }
    }

    private static void copyProperties(final Iterable<BldRunProperty> from, final List<? super BldRunProperty> to) {
        to.clear();
        for (BldRunProperty p : from) {
            to.add(p.clone());
        }
    }
}
