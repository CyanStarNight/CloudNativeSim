/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.extend;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.*;
import org.cloudbus.nativesim.core.Status;
import org.cloudbus.nativesim.policy.cloudletScheduler.NativeCloudletSchedulerTimeShared;
import org.cloudbus.nativesim.provisioner.VmBwProvisioner;
import org.cloudbus.nativesim.provisioner.NativePeProvisioner;
import org.cloudbus.nativesim.provisioner.NativeRamProvisioner;
import org.cloudbus.nativesim.service.Instance;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class NativeVm extends Vm {

    private String uid; // the global id
    private int id;
    private int userId;

    private List<? extends Pe> peList;

    private Long freeStorage;

    private List<Instance> instanceList;

    private List<NativePe> nativePeList;

    private NativePeProvisioner nativePeProvisioner;

    private NativeRamProvisioner nativeRamProvisioner;

    private VmBwProvisioner vmBwProvisioner;

    public void setUid() {
        uid = userId + "-Vm-" + id;
    }

    public static String getUid(int userId, int id) {
        return userId + "-Vm-" + id;
    }
    public NativeVm(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm,
//                    InstanceScheduler instanceScheduler,
                    NativePeProvisioner nativePeProvisioner,
                    NativeRamProvisioner nativeRamProvisioner,
                    VmBwProvisioner vmBwProvisioner){

        super(id, userId, mips, numberOfPes, ram, bw, size, vmm, new CloudletSchedulerTimeShared()); //TODO: 2024/3/6 默认的cloudletscheduler要注意

        setInstanceList(new ArrayList<>());
//        setInstanceScheduler(instanceScheduler);
        setNativePeProvisioner(nativePeProvisioner);
        setNativeRamProvisioner(nativeRamProvisioner);
        setVmBwProvisioner(vmBwProvisioner);
        setFreeStorage(size);

        setNativePeList(new ArrayList<>());
    }

    public boolean instanceCreate(Instance instance, int peId){

        if (getSize() < instance.getSize()) {
            Log.printLine("\n[InstanceScheduler.instanceCreate] Allocation of "+ instance.getType()+" #" + instance.getId() + " to Vm #" + getId()
                    + " failed by storage");
            instance.setStatus(Status.Denied);
            return false;
        }

        if (!getNativeRamProvisioner().allocateRamForInstance(instance, instance.getRequests_ram())) {
            Log.printLine("\n[InstanceScheduler.instanceCreate] Allocation of "+ instance.getType()+" #" + instance.getId() + " to Vm #" + getId()
                    + " failed by RAM");
            instance.setStatus(Status.Denied);
            return false;
        }

        if (!getVmBwProvisioner().allocateBwForInstance(instance, instance.getReceive_bw(), instance.getTransmit_bw())) {
            Log.printLine("\n[InstanceScheduler.instanceCreate] Allocation of "+ instance.getType()+" #" + instance.getId() + " to Vm #" + getId()
                    + " failed by BW");
            getNativeRamProvisioner().deallocateRamForInstance(instance);
            instance.setStatus(Status.Denied);
            return false;
        }


        if (!getNativePeProvisioner().allocatePeForInstance(instance, instance.getRequests_share())) {
            Log.printLine("\n[InstanceScheduler.instanceCreate] Allocation of "+ instance.getType()+" #" + instance.getId() + " to Vm #" + getId()
                    + " failed by PE");
            getNativeRamProvisioner().deallocateRamForInstance(instance);
            getVmBwProvisioner().deallocateBwForInstance(instance);
            instance.setStatus(Status.Denied);
            return false;
        }

        getInstanceList().add(instance);

        // 根据allocated资源去完善instance的属性
        instance.setStatus(Status.Created);
        instance.setVm(this);
        instance.setCloudletScheduler(new NativeCloudletSchedulerTimeShared(instance.getCurrentAllocatedMips(),instance.getCurrentAllocatedCpuShare()));

        setFreeStorage(getFreeStorage() - instance.getSize());

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
            setSize(getSize() + instance.getSize());
        }
        getInstanceList().clear();
    }

    protected void instanceDeallocate(Instance instance) {
        getNativeRamProvisioner().deallocateRamForInstance(instance);
        getVmBwProvisioner().deallocateBwForInstance(instance);
        getNativePeProvisioner().deallocatePeForInstance(instance);
        setSize(getSize() + instance.getSize());
        instance.setVm(null);
    }


    protected void instanceDeallocateAll() {
        getNativeRamProvisioner().deallocateRamForAllInstances();
        getVmBwProvisioner().deallocateBwForAllInstances();
        getNativePeProvisioner().deallocatePesForAllInstances();
    }


}
