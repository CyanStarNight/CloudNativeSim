/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.nativesim.entity.Container;
import org.cloudbus.nativesim.entity.Pod;

@Getter
@Setter
public abstract class NativeBwProvisioner extends BwProvisioner {
    private long bw;

    private long availableBw;

    public NativeBwProvisioner(long bw) {
        super(bw);
        setBw(bw);
        setAvailableBw(bw);
    }

    public abstract boolean allocateBwForContainer(Container container, long bw);

    public abstract long getAllocatedBwForContainer(Container container);

    public abstract void deallocateBwForContainer(Container container);

    public void deallocateBwForAllContainers() {
        setAvailableBw(getBw());
    }
    public abstract boolean isSuitableForContainer(Container container, long bw);

    public long getUsedBw() {
        return bw - availableBw;
    }

//    public boolean allocateBwForPod(Pod pod, long bw){
//        return pod.getContainerList().stream().
//                allMatch(container -> allocateBwForContainer(container,bw));
//    }

    public long getAllocatedBwForPod(Pod pod) {
        return pod.getContainerList().stream()
                .mapToLong(this::getAllocatedBwForContainer)
                .sum();
    }

    public void deallocateBwForPod(Pod pod){
        pod.getContainerList().forEach(this::deallocateBwForContainer);
    }

    public void deallocateBwForAllPods() {
        setAvailableBw(getBw());
    }
    public boolean isSuitableForPod(Pod pod, long bw) {
        return pod.getContainerList().stream().
                allMatch(container -> isSuitableForContainer(container,bw));
    }

}
