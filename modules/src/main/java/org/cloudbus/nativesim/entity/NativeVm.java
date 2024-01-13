/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.entity;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.*;
import org.cloudbus.nativesim.provisioner.NativeBwProvisioner;
import org.cloudbus.nativesim.provisioner.NativePeProvisioner;
import org.cloudbus.nativesim.provisioner.NativeRamProvisioner;
import org.cloudbus.nativesim.scheduler.ContainerScheduler;
import org.cloudbus.nativesim.scheduler.NativeCloudletScheduler;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class NativeVm extends Vm {

    private long storage;
    private List<? extends NativePe> peList;

    private NativeBwProvisioner nativeBwProvisioner;
    private NativeRamProvisioner nativeRamProvisioner;
    private NativePeProvisioner nativePeProvisioner;

    private final List<Container> containerList = new ArrayList<>();
    private final List<Container> containersMigratingIn = new ArrayList<>();
    private final List<Pod> podList = new ArrayList<>();
    private final List<Pod> podsMigratingIn = new ArrayList<>();
    private ContainerScheduler containerScheduler;

    public NativeVm(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm, NativeCloudletScheduler cloudletScheduler) {
        super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
    }

    public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
        if (mipsShare != null) {
            return getCloudletScheduler().updateVmProcessing(currentTime, mipsShare);
        }
        return 0.0;
    }

//    public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
////        Log.printLine("Vm: update Vms Processing at " + currentTime);
//        if (mipsShare != null && !getContainerList().isEmpty()) {
//            double smallerTime = Double.MAX_VALUE;
//
//            for (Container container : getContainerList()) {
//                double time = container.updateContainerProcessing(currentTime, getContainerScheduler().getAllocatedMipsForContainer(container));
//                if (time > 0.0 && time < smallerTime) {
//                    smallerTime = time;
//                }
//            }
//
//            return smallerTime;
//        }
//
//        return 0.0;
//    }

    public boolean containerCreate(Container container) {
        if (getStorage() < container.getSize()) {
            Log.printLine("[ContainerScheduler.containerCreate] Allocation of CONTAINER #" + container.getId() + " to Vm #" + getId()
                    + " failed by storage");
            return false;
        }

        if (!getNativeRamProvisioner().allocateRamForContainer(container, container.getCurrentRequestedRam())) {
            Log.printLine("[ContainerScheduler.containerCreate] Allocation of CONTAINER #" + container.getId() + " to Vm #" + getId()
                    + " failed by RAM");
            return false;
        }

        if (!getNativeBwProvisioner().allocateBwForContainer(container, container.getCurrentRequestedBw())) {
            Log.printLine("[ContainerScheduler.containerCreate] Allocation of CONTAINER #" + container.getId() + " to Vm #" + getId()
                    + " failed by BW");
            getNativeRamProvisioner().deallocateRamForContainer(container);
            return false;
        }

        if (!getContainerScheduler().allocatePesForContainer(container, container.getCurrentRequestedMips())) {
            Log.printLine("[ContainerScheduler.containerCreate] Allocation of CONTAINER #" + container.getId() + " to Vm #" + getId()
                    + " failed by MIPS");
            getNativeRamProvisioner().deallocateRamForContainer(container);
            getNativeBwProvisioner().deallocateBwForContainer(container);
            return false;
        }

        setStorage(getStorage() - container.getSize());
        getContainerList().add(container);
        container.setVm(this);
        return true;
    }

    public boolean podCreate(Pod pod) {
        Log.printLine("[ContainerScheduler.podCreate] Allocation of POD #" + pod.getId() + " to Vm #" + getId());
        pod.getContainerList().forEach(this::containerCreate);
        getPodList().add(pod);
        pod.setVm(this);
        return true;
    }
}
