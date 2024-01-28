/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.entity;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.*;
import org.cloudbus.nativesim.provisioner.InstanceBwProvisioner;
import org.cloudbus.nativesim.provisioner.InstancePeProvisioner;
import org.cloudbus.nativesim.provisioner.InstanceRamProvisioner;
import org.cloudbus.nativesim.scheduler.InstanceScheduler;
import org.cloudbus.nativesim.scheduler.NativeCloudletScheduler;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class NativeVm extends Vm {

    private String uid; // the global id
    private int id;
    private int userId;

    private long storage;
    private List<? extends NativePe> peList;

    private InstanceBwProvisioner instanceBwProvisioner;
    private InstanceRamProvisioner instanceRamProvisioner;
    private InstancePeProvisioner instancePeProvisioner;

    private final List<Instance> instanceList= new ArrayList<>();
    private final List<Container> containerList = new ArrayList<>();
    private final List<Container> containersMigratingIn = new ArrayList<>();
    private final List<Pod> podList = new ArrayList<>();
    private final List<Pod> podsMigratingIn = new ArrayList<>();
    private InstanceScheduler instanceScheduler;

    public void setUid() {
        uid = userId + "-Vm-" + id;
    }

    public static String getUid(int userId, int id) {
        return userId + "-Vm-" + id;
    }
    public NativeVm(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm,
                    InstanceBwProvisioner instanceBwProvisioner,
                    InstanceRamProvisioner instanceRamProvisioner,
                    InstancePeProvisioner instancePeProvisioner,
                    NativeCloudletScheduler cloudletScheduler) {
        super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
        this.instanceBwProvisioner = instanceBwProvisioner;
        this.instanceRamProvisioner = instanceRamProvisioner;
        this.instancePeProvisioner = instancePeProvisioner;
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

    public boolean instanceCreate(Instance instance){
        if (getStorage() < instance.getSize()) {
            Log.printLine("[InstanceScheduler.instanceCreate] Allocation of INSTANCE #" + instance.getId() + " to Vm #" + getId()
                    + " failed by storage");
            return false;
        }

        if (!getInstanceRamProvisioner().allocateRamForInstance(instance, instance.getCurrentRequestedRam())) {
            Log.printLine("[InstanceScheduler.instanceCreate] Allocation of INSTANCE #" + instance.getId() + " to Vm #" + getId()
                    + " failed by RAM");
            return false;
        }

        if (!getInstanceBwProvisioner().allocateBwForInstance(instance, instance.getCurrentRequestedBw())) {
            Log.printLine("[InstanceScheduler.instanceCreate] Allocation of INSTANCE #" + instance.getId() + " to Vm #" + getId()
                    + " failed by BW");
            getInstanceRamProvisioner().deallocateRamForInstance(instance);
            return false;
        }

        if (!getInstanceScheduler().allocatePesForInstance(instance, instance.getCurrentRequestedMips())) {
            Log.printLine("[InstanceScheduler.instanceCreate] Allocation of INSTANCE #" + instance.getId() + " to Vm #" + getId()
                    + " failed by MIPS");
            getInstanceRamProvisioner().deallocateRamForInstance(instance);
            getInstanceBwProvisioner().deallocateBwForInstance(instance);
            return false;
        }

        String class_name = instance.getClass().getSimpleName();
        switch (class_name){
            case "Pod":
                assert instance instanceof Pod;
                getPodList().add((Pod) instance);
            case "Container":
                assert instance instanceof Container;
                getContainerList().add((Container) instance);
            default: break;
        }

        setStorage(getStorage() - instance.getSize());
        instance.setVm(this);

        return true;

    }

    public void instanceDestroy(Instance instance) {
        if (instance != null) {
            instanceDeallocate(instance);
            getInstanceList().remove(instance);
            instance.setVm(null);
        }
    }

    public void instanceDestroyAll() {
        instanceDeallocateAll();
        for (Instance instance : getInstanceList()) {
            instance.setVm(null);
            setStorage(getStorage() + instance.getSize());
        }
        getInstanceList().clear();
    }

    protected void instanceDeallocate(Instance instance) {
        getInstanceRamProvisioner().deallocateRamForInstance(instance);
        getInstanceBwProvisioner().deallocateBwForInstance(instance);
        getInstanceScheduler().deallocatePesForInstance(instance);
        setStorage(getStorage() + instance.getSize());
    }


    protected void instanceDeallocateAll() {
        getInstanceRamProvisioner().deallocateRamForAllInstances();
        getInstanceBwProvisioner().deallocateBwForAllInstances();
        getInstanceScheduler().deallocatePesForAllInstances();
    }


    public Instance getInstance(int instanceId, int userId) {
        for (Instance instance : getInstanceList()) {
            if (instance.getId() == instanceId && instance.getUserId() == userId) {
                return instance;
            }
        }
        return null;
    }
}
