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

    private final List<Container> containersMigratingIn = new ArrayList<>();
    private NativeBwProvisioner containerBwProvisioner;
    private NativeRamProvisioner containerRamProvisioner;
    private NativePeProvisioner containerPeProvision;
    private final List<? extends Container> containerList = new ArrayList<>();
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
   


}
