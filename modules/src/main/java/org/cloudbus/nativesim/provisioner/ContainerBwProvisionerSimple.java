/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.nativesim.entity.Instance;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ContainerBwProvisionerSimple extends InstanceBwProvisioner {
    private Map<String, Long> containerBwTable;
    private final String instanceType = "Container";

    public ContainerBwProvisionerSimple(long bw) {
        super(bw);
        setContainerBwTable(new HashMap<String, Long>());
    }
    @Override
    public boolean allocateBwForInstance(Instance container, long bw) {
        deallocateBwForInstance(container);

        if (getAvailableBw() >= bw) {
            setAvailableBw(getAvailableBw() - bw);
            getContainerBwTable().put(container.getUid(), bw);
            container.setCurrentAllocatedBw(getAllocatedBwForInstance(container));
            return true;
        }

        container.setCurrentAllocatedBw(getAllocatedBwForInstance(container));
        return false;
    }
    @Override
    public long getAllocatedBwForInstance(Instance container) {
        if (getContainerBwTable().containsKey(container.getUid())) {
            return getContainerBwTable().get(container.getUid());
        }
        return 0;
    }

    @Override
    public void deallocateBwForInstance(Instance container) {
        if (getContainerBwTable().containsKey(container.getUid())) {
            long amountFreed = getContainerBwTable().remove(container.getUid());
            setAvailableBw(getAvailableBw() + amountFreed);
            container.setCurrentAllocatedBw(0);
        }
    }

    public void deallocateBwForAllContainers() {
        setAvailableBw(getTotalBw());
        getContainerBwTable().clear();
    }
    @Override
    public boolean isSuitableForInstance(Instance container, long bw) {
        long allocatedBw = getAllocatedBwForInstance(container);
        boolean result = allocateBwForInstance(container, bw);
        deallocateBwForInstance(container);
        if (allocatedBw > 0) {
            allocateBwForInstance(container, allocatedBw);
        }
        return result;
    }


}
