package org.cloudbus.nativesim.policy.allocation;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.nativesim.entity.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public abstract class ServiceAllocationPolicy {
    private List<? extends NativeVm> vmList;

    public ServiceAllocationPolicy(List<? extends NativeVm> vmList) {
        setVmList(vmList);
    }

    @SuppressWarnings("unchecked")
    public <T extends NativeVm> List<T> getVmList() {
        return (List<T>) vmList;
    }
    
    public abstract boolean allocateService(Service service);

    public abstract List<Map<String, Object>> optimizeAllocation(List<? extends Service> serviceList);

    public void deallocateService(Service service){
        service.getInstanceList().forEach(this::deallocateVmForInstance);
    }

    /** Container Allocation Policy*/
    public abstract boolean allocateVmForInstance(Instance instance);

    public abstract void deallocateVmForInstance(Instance instance);

    public abstract NativeVm getVm(Instance instance);

}
