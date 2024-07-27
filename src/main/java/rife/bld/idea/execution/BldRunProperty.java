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

@Tag("build-property")
public final class BldRunProperty implements JDOMExternalizable, Cloneable {
    @NonNls  private static final String NAME = "bld.name";
    @NonNls private static final String VALUE = "value";
    private String propertyName_;
    private String propertyValue_;

    public BldRunProperty() {
        this("", "");
    }

    public BldRunProperty(String propertyName, String propertyValue) {
        setPropertyName(propertyName);
        propertyValue_ = propertyValue;
    }

    @Attribute(NAME)
    public String getPropertyName() {
        return propertyName_;
    }

    public void setPropertyName(String propertyName) {
        propertyName_ = propertyName.trim();
    }

    @Attribute(VALUE)
    public String getPropertyValue() {
        return propertyValue_;
    }

    public void setPropertyValue(String propertyValue) {
        propertyValue_ = propertyValue;
    }

    @Override
    public void readExternal(Element element) {
        propertyName_ = element.getAttributeValue(NAME);
        propertyValue_ = element.getAttributeValue(VALUE);
    }

    @Override
    public void writeExternal(Element element) {
        element.setAttribute(NAME, getPropertyName());
        element.setAttribute(VALUE, getPropertyValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var that = (BldRunProperty)o;
        return Objects.equals(propertyName_, that.propertyName_) && Objects.equals(propertyValue_, that.propertyValue_);
    }

    @Override
    public int hashCode() {
        return 31 * (propertyName_ != null ? propertyName_.hashCode() : 0)
            + (propertyValue_ != null ? propertyValue_.hashCode() : 0);
    }

    @Override
    public BldRunProperty clone() {
        try {
            return (BldRunProperty)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
