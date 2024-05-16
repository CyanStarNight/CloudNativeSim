/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.migration;

import lombok.Getter;
import lombok.Setter;
import entity.Instance;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class InstanceMigrationPolicy {

    /** The instances migrating in. */
    private List<String> instancesMigratingIn;

    /** The instances migrating out. */
    private List<String> instancesMigratingOut;

    public InstanceMigrationPolicy() {
        setInstancesMigratingIn(new ArrayList<>());
        setInstancesMigratingOut(new ArrayList<>());
    }

    public static boolean needMigrate(Instance instance) {
        return false;
    }

    protected abstract void updatePeProvisioning();

    public void migrateInstance(Instance instanceToMigrate) {
    }
}
