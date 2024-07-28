/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.utils.BldBundle;

import javax.swing.*;
import java.awt.*;

public class BldRunConfigurationSettingsEditor extends SettingsEditor<RunConfiguration> {
    private final BldRunConfiguration runConfiguration_;
    private String commandName_ = null;
    private ExtendableTextField textField_;
    private final BldRunConfigurationOptionsTable optionsTable_ = new BldRunConfigurationOptionsTable();
    private final BldRunConfigurationPropertiesTable propertiesTable_ = new BldRunConfigurationPropertiesTable();

    public BldRunConfigurationSettingsEditor(BldRunConfiguration runConfiguration) {
        runConfiguration_ = runConfiguration;
    }

    private void updateUI() {
        textField_.setText("");
        if (commandName_ != null) {
            textField_.setText(commandName_);
        }
        optionsTable_.refreshValues();
        propertiesTable_.refreshValues();
        fireEditorStateChanged();
    }

    @Override
    protected void resetEditorFrom(@NotNull RunConfiguration s) {
        final var config = (BldRunConfiguration) s;
        commandName_ = config.settings_.commandName_;
        optionsTable_.setValues(config.settings_.options_);
        propertiesTable_.setValues(config.settings_.properties_);
        updateUI();
    }

    @Override
    protected void applyEditorTo(@NotNull RunConfiguration s) {
        final var config = (BldRunConfiguration) s;
        config.settings_.commandName_ = commandName_;
        BldRunConfiguration.copyOptions(ContainerUtil.filter(optionsTable_.getElements(), option -> !optionsTable_.isEmpty(option)), config.settings_.options_);
        BldRunConfiguration.copyProperties(ContainerUtil.filter(propertiesTable_.getElements(), property -> !propertiesTable_.isEmpty(property)), config.settings_.properties_);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        textField_ = new ExtendableTextField().addBrowseExtension(() -> {
            var command = runConfiguration_.getCommand();
            final BldCommandChooserDialog dlg = new BldCommandChooserDialog(runConfiguration_.getProject(), command);
            if (dlg.showAndGet()) {
                commandName_ = null;
                command = dlg.getSelectedCommand();
                if (command != null) {
                    commandName_ = command.name();
                }
                updateUI();
            }
        }, this);

        final var panel = new JPanel(new BorderLayout());
        panel.add(LabeledComponent.create(textField_, BldBundle.message("bld.label.run.config.command.name"), BorderLayout.WEST), BorderLayout.NORTH);

        final var options_table_name = BldBundle.message("bld.label.table.name.options");
        final var options_table_component = LabeledComponent.create(optionsTable_.getComponent(), options_table_name);
        options_table_component.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(options_table_component, BorderLayout.CENTER);

        final var properties_table_name = BldBundle.message("bld.label.table.name.properties");
        final var properties_table_component = LabeledComponent.create(propertiesTable_.getComponent(), properties_table_name);
        properties_table_component.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(properties_table_component, BorderLayout.SOUTH);

        return panel;
    }
}
