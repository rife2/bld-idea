package rife.bld.idea.service.project.metadata;


import com.intellij.openapi.externalSystem.model.Key;
import com.intellij.openapi.externalSystem.model.ProjectKeys;
import com.intellij.openapi.externalSystem.model.project.ModuleData;
import com.intellij.serialization.PropertyMapping;
import org.jetbrains.annotations.NotNull;
import rife.bld.idea.model.TargetAddressInfo;
import rife.bld.idea.utils.BldConstants;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TargetMetadata extends ModuleData {
    @Serial
    private static final long serialVersionUID = 1146131614006594622L;

    @NotNull
    public static final Key<TargetMetadata> KEY =
        Key.create(TargetMetadata.class, ProjectKeys.MODULE.getProcessingWeight() + 1);

    private Set<String> myLibraryExcludes = Collections.emptySet();
    private Set<String> myTargetAddresses = Collections.emptySet();
    private Set<TargetAddressInfo> myTargetAddressInfoSet = Collections.emptySet();

    @PropertyMapping({"id", "moduleTypeId", "externalName", "moduleFileDirectoryPath", "externalConfigPath"})
    public TargetMetadata(
        @NotNull String id,
        @NotNull String moduleTypeId,
        @NotNull String externalName,
        @NotNull String moduleFileDirectoryPath,
        @NotNull String externalConfigPath
    ) {
        super(id, BldConstants.SYSTEM_ID, moduleTypeId,
            externalName, moduleFileDirectoryPath, externalConfigPath
        );
        setModuleName(externalName);
    }

    @NotNull
    public Set<String> getTargetAddresses() {
        return myTargetAddresses;
    }

    public void setTargetAddresses(Collection<String> targetAddresses) {
        myTargetAddresses = new HashSet<>(targetAddresses);
    }

    public Set<TargetAddressInfo> getTargetAddressInfoSet() {
        return myTargetAddressInfoSet;
    }

    public void setTargetAddressInfoSet(Set<TargetAddressInfo> targetAddressInfoSet) {
        myTargetAddressInfoSet = new HashSet<>(targetAddressInfoSet);
    }

    @NotNull
    public Set<String> getLibraryExcludes() {
        return myLibraryExcludes;
    }

    public void setLibraryExcludes(Set<String> libraryExcludes) {
        myLibraryExcludes = libraryExcludes;
    }
}
