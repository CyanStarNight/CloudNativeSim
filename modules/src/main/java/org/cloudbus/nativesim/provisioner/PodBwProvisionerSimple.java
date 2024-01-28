/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.nativesim.entity.Container;
import org.cloudbus.nativesim.entity.Instance;
import org.cloudbus.nativesim.entity.Pod;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PodBwProvisionerSimple extends InstanceBwProvisioner {
    private Map<String, Long> podBwTable;
    private Map<String, Long> containerBwTable;
    private final String instanceType = "Pod";

    public PodBwProvisionerSimple(long bw) {
        super(bw);
        setContainerBwTable(new HashMap<String, Long>());
        setPodBwTable(new HashMap<String, Long>());
    }

    @Override
    public boolean allocateBwForInstance(Instance pod, long bw){
        deallocateBwForInstance(pod);
        if (getAvailableBw() >= bw) {
            setAvailableBw(getAvailableBw() - bw);
            getPodBwTable().put(pod.getUid(), bw);
            for(Container container :((Pod) pod).getContainerList()){
                long containerBw = container.getCurrentRequestedBw();
                bw -= containerBw;
                getContainerBwTable().put(container.getUid(), containerBw);
                container.setCurrentAllocatedBw(getAllocatedBwForInstance(container));
            }
            pod.setCurrentAllocatedBw(getAllocatedBwForInstance(pod));
            return true;
        }

        getPodBwTable().put(pod.getUid(), bw);
        return false;
    }
    @Override
    public long getAllocatedBwForInstance(Instance pod) {
        if (getPodBwTable().containsKey(pod.getUid())) {
            return getContainerBwTable().get(pod.getUid());
        }
        return 0;
    }
    @Override
    public void deallocateBwForInstance(Instance pod){
        if (getPodBwTable().containsKey(pod.getUid())) {
            for(Container container :((Pod) pod).getContainerList()){
                if (getContainerBwTable().containsKey(container.getUid())) {
                    getContainerBwTable().remove(container.getUid());
                    container.setCurrentAllocatedBw(0);
                }
            }
            long amountFreed = getPodBwTable().remove(pod.getUid());
            setAvailableBw(getAvailableBw() + amountFreed);
            pod.setCurrentAllocatedBw(0);
        }
    }

    @Override
    public boolean isSuitableForInstance(Instance pod, long bw) {
        long allocatedBw = getAllocatedBwForInstance(pod);
        boolean result = allocateBwForInstance(pod, bw);
        deallocateBwForInstance(pod);
        if (allocatedBw > 0) {
            allocateBwForInstance(pod, allocatedBw);
        }
        return result;
    }
}
