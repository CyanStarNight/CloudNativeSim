/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.*;
import org.cloudbus.nativesim.Log;
import org.cloudbus.nativesim.provisioner.NativeBwProvisioner;
import org.cloudbus.nativesim.provisioner.NativePeProvisioner;
import org.cloudbus.nativesim.provisioner.NativeRamProvisioner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
@Getter
@Setter
public class NativeVm extends Vm {

    private List<? extends Pe> peList;

    private boolean inMigration;
    private boolean inWaiting;
    private long currentAllocatedSize;
    private float currentAllocatedRam;
    private long currentAllocatedBw;
    private List<Double> currentAllocatedMips;
    private boolean beingInstantiated;
    private final List<Container> containersMigratingIn = new ArrayList<>();
    private final List<VmStateHistoryEntry> stateHistory = new LinkedList<VmStateHistoryEntry>();

    private NativeBwProvisioner containerBwProvisioner;
    private NativeRamProvisioner containerRamProvisioner;
    private NativePeProvisioner containerPeProvision;
    private final List<? extends Container> containerList = new ArrayList<>();

    private NativeBwProvisioner podBwProvisioner;
    private NativeRamProvisioner podRamProvisioner;
    private NativePeProvisioner podPeProvision;
    private final List<? extends Pod> podList = new ArrayList<>();


    public NativeVm(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
        super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
    }

    public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
//        Log.printLine("Vm: update Vms Processing at " + currentTime);
        if (mipsShare != null && !getContainerList().isEmpty()) {
            double smallerTime = Double.MAX_VALUE;
//            Log.printLine("ContainerVm: update Vms Processing");
//            Log.printLine("The VM list size is:...." + getContainerList().size());

            for (Container container : getContainerList()) {
                double time = container.updateContainerProcessing(currentTime, getContainerScheduler().getAllocatedMipsForContainer(container));
                if (time > 0.0 && time < smallerTime) {
                    smallerTime = time;
                }
            }
//            Log.printLine("ContainerVm: The Smaller time is:......" + smallerTime);

            return smallerTime;
        }
//        if (mipsShare != null) {
//            return getContainerScheduler().updateVmProcessing(currentTime, mipsShare);
//        }
        return 0.0;
    }
    public double getTotalUtilizationOfCpu(double time) {
        float TotalUtilizationOfCpu = 0;

        for (Container container : getContainerList()) {
            TotalUtilizationOfCpu += container.getTotalUtilizationOfCpu(time);
        }

        //Log.printLine("Vm: get Current requested Mips" + TotalUtilizationOfCpu);
        return TotalUtilizationOfCpu;


    }

    public double getTotalUtilizationOfCpuMips(double time) {
        //Log.printLine("Container: get Current getTotalUtilizationOfCpuMips" + getTotalUtilizationOfCpu(time) * getMips());
        return getTotalUtilizationOfCpu(time) * getMips();
    }
    public void containerDestroy(Container container) {
        //Log.printLine("Vm:  Destroy Container:.... " + container.getId());
        if (container != null) {
            containerDeallocate(container);
//            Log.printConcatLine("The Container To remove is :   ", container.getId(), "Size before removing is ", getContainerList().size(), "  vm ID is: ", getId());
            getContainerList().remove(container);
            Log.printLine("ContainerVm# "+getId()+" containerDestroy:......" + container.getId() + "Is deleted from the list");

//            Log.printConcatLine("Size after removing", getContainerList().size());
            while(getContainerList().contains(container)){
                Log.printConcatLine("The container", container.getId(), " is still here");
//                getContainerList().remove(container);
            }
            container.setVm(null);
        }
    }


}
