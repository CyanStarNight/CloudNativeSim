/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.nativesim.entity.Container;
import org.cloudbus.nativesim.entity.Pod;

@Getter
@Setter
public abstract class NativeRamProvisioner extends RamProvisioner{
    private int ram;

    private int availableRam;

    public NativeRamProvisioner(int ram) {
        super(ram);
        setRam(ram);
        setAvailableRam(ram);
    }

    public abstract boolean allocateRamForContainer(Container container, int ram);

    public abstract int getAllocatedRamForContainer(Container container);

    public abstract void deallocateRamForContainer(Container container);

    public void deallocateRamForAllEntities() {
        setAvailableRam(getRam());
    }
    public abstract boolean isSuitableForContainer(Container container, int ram);

    public int getUsedRam() {
        return ram - availableRam;
    }

//    public boolean allocateRamForPod(Pod pod, int ram){
//        return pod.getContainerList().stream().
//                allMatch(container -> allocateRamForContainer(container,ram));
//    }

    public long getAllocatedRamForPod(Pod pod) {
        return pod.getContainerList().stream()
                .mapToLong(this::getAllocatedRamForContainer)
                .sum();
    }

    public void deallocateRamForPod(Pod pod){
        pod.getContainerList().forEach(this::deallocateRamForContainer);
    }

    public void deallocateRamForAllPods() {
        setAvailableRam(getRam());
    }
    public boolean isSuitableForPod(Pod pod, int ram) {
        return pod.getContainerList().stream().
                allMatch(container -> isSuitableForContainer(container,ram));
    }
}
