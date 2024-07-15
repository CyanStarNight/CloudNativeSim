/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.scaling;

import entity.Instance;
import entity.Service;
import extend.NativeVm;
import lombok.Getter;
import lombok.Setter;
import policy.allocation.ServiceAllocationPolicy;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class ServiceScalingPolicy {

    private ServiceAllocationPolicy serviceAllocationPolicy;

    private List<NativeVm> vmList;

    private List<Instance> newInstances;


    public ServiceScalingPolicy() {
        newInstances = new ArrayList<>();
    }

    public ServiceScalingPolicy(Service service,ServiceAllocationPolicy serviceAllocationPolicy) {
        this.vmList = serviceAllocationPolicy.getVmList();
        newInstances = new ArrayList<>();
    }

    public abstract boolean needScaling(Service service);

    public abstract void scaling(Service service);


}
