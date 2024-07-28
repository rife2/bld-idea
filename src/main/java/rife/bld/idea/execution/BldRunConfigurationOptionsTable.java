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

public class BldRunConfigurationOptionsTable extends ListTableWithButtons<BldRunOption> {
    @Override
    protected ListTableModel<BldRunOption> createListModel() {
        final ColumnInfo<BldRunOption, @NlsContexts.ListItem String> nameColumn = new TableColumn(BldBundle.message("bld.column.name.config.option.name")) {
            @Nullable
            @Override
            public String valueOf(BldRunOption option) {
                return option.getOptionName();
            }

            @Override
            public void setValue(BldRunOption option, String value) {
                option.setOptionName(value);
            }
        };
        return new ListTableModel<>(nameColumn);
    }

    @Override
    protected BldRunOption createElement() {
        return new BldRunOption();
    }

    @Override
    protected boolean isEmpty(BldRunOption element) {
        return StringUtil.isEmpty(element.getOptionName());
    }

    @Override
    protected BldRunOption cloneElement(BldRunOption p) {
        return p.clone();
    }

    @Override
    protected boolean canDeleteElement(BldRunOption selection) {
        return true;
    }

    @Override
    public List<BldRunOption> getElements() {
        return super.getElements();
    }

    private abstract static class TableColumn extends ElementsColumnInfoBase<BldRunOption> {
        TableColumn(final @NlsContexts.ColumnName String name) {
            super(name);
        }

        @Override
        public boolean isCellEditable(BldRunOption option) {
            return true;
        }

        @Nullable
        @Override
        protected String getDescription(BldRunOption element) {
            return null;
        }
    }
}
