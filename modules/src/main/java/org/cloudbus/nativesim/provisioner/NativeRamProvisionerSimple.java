/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.nativesim.service.Instance;

import java.util.HashMap;
import java.util.Map;
@Getter
@Setter
public class NativeRamProvisionerSimple extends NativeRamProvisioner {

    private Map<String, Integer> instanceRamTable;

    public NativeRamProvisionerSimple() {
        setInstanceRamTable(new HashMap<String, Integer>());
    }


    @Override
    public boolean allocateRamForInstance(Instance instance, int ram) {
        int maxRam = instance.getRequests_ram();

        if (ram >= maxRam) {
            ram = maxRam;
        }

        deallocateRamForInstance(instance);

        if (getAvailableRam() >= ram) {
            setAvailableRam(getAvailableRam() - ram);
            getInstanceRamTable().put(instance.getUid(), ram);
            instance.setCurrentAllocatedRam(getAllocatedRamForInstance(instance));
            return true;
        }

        instance.setCurrentAllocatedRam(getAllocatedRamForInstance(instance));

        return false;
    }
    @Override
    public int getAllocatedRamForInstance(Instance instance) {
        if (getInstanceRamTable().containsKey(instance.getUid())) {
            return getInstanceRamTable().get(instance.getUid());
        }
        return 0;
    }

    @Override
    public void deallocateRamForInstance(Instance instance) {
        if (getInstanceRamTable().containsKey(instance.getUid())) {
            int amountFreed = getInstanceRamTable().remove(instance.getUid());
            setAvailableRam(getAvailableRam() + amountFreed);
            instance.setCurrentAllocatedRam(0);
        }
    }

    public void deallocateRamForAllInstances() {
        setAvailableRam(getRam());
        getInstanceRamTable().clear();
    }
    @Override
    public boolean isSuitableForInstance(Instance instance, int ram) {
        int allocatedRam = getAllocatedRamForInstance(instance);
        boolean result = allocateRamForInstance(instance, ram);
        deallocateRamForInstance(instance);
        if (allocatedRam > 0) {
            allocateRamForInstance(instance, allocatedRam);
        }
        return result;
    }


}
