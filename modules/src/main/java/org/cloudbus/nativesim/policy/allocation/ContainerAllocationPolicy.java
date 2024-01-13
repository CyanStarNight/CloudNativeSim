package org.cloudbus.nativesim.policy.allocation;

import org.cloudbus.nativesim.entity.NativeVm;
import org.cloudbus.nativesim.entity.Container;
import org.cloudbus.nativesim.entity.Pod;
import org.cloudbus.nativesim.entity.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ContainerAllocationPolicy {
    private List<? extends NativeVm> vmList;

    public ContainerAllocationPolicy(List<? extends NativeVm> list) {
        setVmList(list);
    }
    protected void setVmList(List<? extends NativeVm> vmList) {
        this.vmList = vmList;
    }

    @SuppressWarnings("unchecked")
    public <T extends NativeVm> List<T> getVmList() {
        return (List<T>) vmList;
    }
    public <T extends NativeVm> List<T> getVmList(Service service){
        List<T> tmpVmList = new ArrayList<>();
        for (Pod pod : service.getPods()){
            tmpVmList.add((T) pod.getVm());
        }
        return tmpVmList;
    }

    /** Container Allocation Policy*/
    public abstract boolean allocateVmForContainer(Container container);

    public abstract boolean allocateVmForContainer(Container container, NativeVm vm);

    public abstract List<Map<String, Object>> optimizeContainerAllocation(List<? extends Container> containerList);

    public abstract void deallocateVmForContainer(Container container);

    public abstract NativeVm getVm(Container container);

    public abstract NativeVm getVmForContainer(int containerId, int userId);

    /** Pod Allocation Policy*/
    public abstract boolean allocateVmForPod(Pod pod);


    public boolean allocateVmForPod(Pod pod, NativeVm vm){
        return pod.getContainerList().stream().allMatch(container -> allocateVmForContainer(container, vm));
    }

    public abstract List<Map<String, Object>> optimizePodAllocation(List<? extends Pod> podList);

    public void deallocateVmForPod(Pod pod){
        pod.getContainerList().forEach(this::deallocateVmForContainer);
    }

    public abstract NativeVm getVm(Pod pod);

    public abstract NativeVm getVmForPod(int podId, int userId);

    /** Service Allocation Policy*/
    public abstract boolean allocateService(Service service);

    public abstract List<Map<String, Object>> optimizeAllocation(List<? extends Service> serviceList);

    public void deallocateService(Service service){
        service.getContainerList().forEach(this::deallocateVmForContainer);
    }

}
