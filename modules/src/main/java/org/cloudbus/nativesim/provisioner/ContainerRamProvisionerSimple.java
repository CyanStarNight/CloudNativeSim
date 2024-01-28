/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.nativesim.entity.Container;
import org.cloudbus.nativesim.entity.Instance;

import java.util.HashMap;
import java.util.Map;
@Getter
@Setter
public class ContainerRamProvisionerSimple extends InstanceRamProvisioner {
    private Map<String, Integer> containerRamTable;

    public ContainerRamProvisionerSimple(int ram) {
        super(ram);
        setContainerRamTable(new HashMap<String, Integer>());
    }

    /* If the requested amount of RAM to be allocated to the ENTITY is greater than
the amount of ENTITY is in fact requiring, allocate only the
amount defined in the Container requirements.*/
    @Override
    public boolean allocateRamForInstance(Instance container, int ram) {
        int maxRam = container.getRam();

        if (ram >= maxRam) {
            ram = maxRam;
        }

        deallocateRamForInstance(container);

        if (getAvailableRam() >= ram) {
            setAvailableRam(getAvailableRam() - ram);
            getContainerRamTable().put(container.getUid(), ram);
            container.setCurrentAllocatedRam(getAllocatedRamForInstance(container));
            return true;
        }

        container.setCurrentAllocatedRam(getAllocatedRamForInstance(container));

        return false;
    }
    @Override
    public int getAllocatedRamForInstance(Instance container) {
        if (getContainerRamTable().containsKey(container.getUid())) {
            return getContainerRamTable().get(container.getUid());
        }
        return 0;
    }

    @Override
    public void deallocateRamForInstance(Instance container) {
        if (getContainerRamTable().containsKey(container.getUid())) {
            int amountFreed = getContainerRamTable().remove(container.getUid());
            setAvailableRam(getAvailableRam() + amountFreed);
            container.setCurrentAllocatedRam(0);
        }
    }

    public void deallocateRamForAllContainers() {
        setAvailableRam(getRam());
        getContainerRamTable().clear();
    }
    @Override
    public boolean isSuitableForInstance(Instance container, int ram) {
        int allocatedRam = getAllocatedRamForInstance(container);
        boolean result = allocateRamForInstance(container, ram);
        deallocateRamForInstance(container);
        if (allocatedRam > 0) {
            allocateRamForInstance(container, allocatedRam);
        }
        return result;
    }


}
