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
public class PodRamProvisionerSimple extends InstanceRamProvisioner {
    private Map<String, Integer> podRamTable;
    private Map<String, Integer> containerRamTable;
    private final String instanceType = "Pod";

    public PodRamProvisionerSimple(int ram) {
        super(ram);
        setContainerRamTable(new HashMap<String, Integer>());
        setPodRamTable(new HashMap<String, Integer>());
    }

    @Override
    public boolean allocateRamForInstance(Instance pod, int ram){
        int maxRam = pod.getRam();
        if (ram >= maxRam) {
            ram = maxRam;
        }
        deallocateRamForInstance(pod);

        if (getAvailableRam() >= ram) {
            setAvailableRam(getAvailableRam() - ram);
            getPodRamTable().put(pod.getUid(), ram);
            for(Container container :((Pod) pod).getContainerList()){
                int containerRam = container.getCurrentRequestedRam();
                ram -= containerRam;
                getContainerRamTable().put(container.getUid(), containerRam);
                container.setCurrentAllocatedRam(getAllocatedRamForInstance(container));
            }
            pod.setCurrentAllocatedRam(getAllocatedRamForInstance(pod));
            return true;
        }

        getPodRamTable().put(pod.getUid(), ram);
        return false;
    }
    @Override
    public int getAllocatedRamForInstance(Instance pod) {
        if (getPodRamTable().containsKey(pod.getUid())) {
            return getContainerRamTable().get(pod.getUid());
        }
        return 0;
    }
    @Override
    public void deallocateRamForInstance(Instance pod){
        if (getPodRamTable().containsKey(pod.getUid())) {
            for(Container container :((Pod) pod).getContainerList()){
                if (getContainerRamTable().containsKey(container.getUid())) {
                    getContainerRamTable().remove(container.getUid());
                    container.setCurrentAllocatedRam(0);
                }
            }
            int amountFreed = getPodRamTable().remove(pod.getUid());
            setAvailableRam(getAvailableRam() + amountFreed);
            pod.setCurrentAllocatedRam(0);
        }
    }
    public void deallocateRamForAllPods() {
        setAvailableRam(getRam());
        getPodRamTable().clear();
    }
    @Override
    public boolean isSuitableForInstance(Instance pod, int ram) {
        int allocatedRam = getAllocatedRamForInstance(pod);
        boolean result = allocateRamForInstance(pod, ram);
        deallocateRamForInstance(pod);
        if (allocatedRam > 0) {
            allocateRamForInstance(pod, allocatedRam);
        }
        return result;
    }
}
