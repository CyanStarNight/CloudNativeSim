/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.allocation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import extend.NativeVm;
import entity.Instance;
import entity.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
public abstract class ServiceAllocationPolicy {

    private List<? extends NativeVm> vmList;

    protected List<Instance> createdInstanceList = new ArrayList<Instance>();

    public void init(List<? extends NativeVm> vmList){
        setVmList(vmList);
    }

    public abstract boolean instantiateService(Service service);
    
    public abstract boolean allocateService(Service service);

    public abstract void deallocateService(Service service);

    /** Instance Allocation Policy*/
    public abstract boolean allocateVmForInstance(Instance instance);

    public abstract void deallocateVmForInstance(Instance instance);

    public abstract NativeVm getVm(Instance instance);

    @SuppressWarnings("unchecked")
    public <T extends NativeVm> List<T> getVmList() {
        return (List<T>) vmList;
    }

    public abstract List<Map<String, Object>> optimizeAllocation(List<? extends Service> serviceList);

}
