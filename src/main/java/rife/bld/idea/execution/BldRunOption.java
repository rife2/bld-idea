/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;

import java.util.Objects;

@Tag("build-option")
public final class BldRunOption implements JDOMExternalizable, Cloneable {
    @NonNls  private static final String NAME = "bld.name";
    private String optionName_;

    public BldRunOption() {
        this("");
    }

    public BldRunOption(String optionName) {
        setOptionName(optionName);
    }

    @Attribute(NAME)
    public String getOptionName() {
        return optionName_;
    }

    public void setOptionName(String optionName) {
        optionName_ = optionName.trim();
    }

    @Override
    public void readExternal(Element element) {
        optionName_ = element.getAttributeValue(NAME);
    }

    @Override
    public void writeExternal(Element element) {
        element.setAttribute(NAME, getOptionName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var that = (BldRunOption)o;
        return Objects.equals(optionName_, that.optionName_);
    }

    @Override
    public int hashCode() {
        return 31 * (optionName_ != null ? optionName_.hashCode() : 0);
    }

    @Override
    public BldRunOption clone() {
        try {
            return (BldRunOption)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
