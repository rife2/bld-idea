/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.Nullable;
import rife.bld.idea.utils.BldBundle;

import java.util.List;

public class BldRunConfigurationPropertiesTable extends ListTableWithButtons<BldRunProperty> {
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
