/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.nativesim.entity.Container;
import org.cloudbus.nativesim.entity.Pod;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class NativePeProvisioner extends PeProvisioner {

    private double mips;

    private double availableMips;

    public NativePeProvisioner(double mips) {
        super(mips);
        setMips(mips);
        setAvailableMips(mips);
    }

    public abstract boolean allocateMipsForContainer(Container container, double mips);
    public abstract boolean allocateMipsForContainer(String containerUid, double mips);
    public abstract boolean allocateMipsForContainer(Container container, List<Double> mips);

    public abstract List<Double> getAllocatedMipsForContainer(Container container);

    public abstract double getTotalAllocatedMipsForContainer(Container container);

    public abstract double getAllocatedMipsForContainerByVirtualPeId(Container container, int peId);

    public abstract void deallocateMipsForContainer(Container container);

    public void deallocateMipsForAllContainers() {
        setAvailableMips(getMips());
    }

    public double getTotalAllocatedMips() {
        double totalAllocatedMips = getMips() - getAvailableMips();
        if (totalAllocatedMips > 0) {
            return totalAllocatedMips;
        }
        return 0;
    }

    public double getUtilization() {
        return getTotalAllocatedMips() / getMips();
    }

    // allocate mips for containers with the same mips
//    public boolean allocateMipsForPod(Pod pod, double mips){
//        return pod.getContainerList().stream().
//                allMatch(container -> allocateMipsForContainer(container,mips));
//    }
//    public boolean allocateMipsForPod(Pod pod, List<Double> mips){
//        return pod.getContainerList().stream().
//                allMatch(container -> allocateMipsForContainer(container,mips));
//    }

    public List<Double> getAllocatedMipsForPod(Pod pod) {
        return pod.getContainerList().stream()
                .map(this::getTotalAllocatedMipsForContainer)
                .collect(Collectors.toList());
    }

    public double getTotalAllocatedMipsForPod(Pod pod){
        return pod.getContainerList().stream()
                .mapToDouble(this::getTotalAllocatedMipsForContainer)
                .sum();
    }

    public double getAllocatedMipsForPodByVirtualPeId(Pod pod, int peId){
        return pod.getContainerList().stream()
                .mapToDouble(container -> getAllocatedMipsForContainerByVirtualPeId(container,peId))
                .sum();
    }

    public void deallocateMipsForPod(Pod pod){
        pod.getContainerList().forEach(this::deallocateMipsForContainer);
    }

    public void deallocateMipsForAllPods() {
        setAvailableMips(getMips());
    }
}
