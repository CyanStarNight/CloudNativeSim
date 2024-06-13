/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.scaling;

import entity.Instance;
import entity.ReplicaSet;
import entity.Service;
import extend.NativeVm;
import lombok.Getter;
import lombok.Setter;
import policy.allocation.ServiceAllocationPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public abstract class ServiceScalingPolicy {

    private ServiceAllocationPolicy serviceAllocationPolicy;

    private List<NativeVm> vmList;

    // scaling失败

    private List<Instance> replications;


    public ServiceScalingPolicy() {
        replications = new ArrayList<>();
    }

    public ServiceScalingPolicy(Service service,ServiceAllocationPolicy serviceAllocationPolicy) {
        this.vmList = serviceAllocationPolicy.getVmList();
        replications = new ArrayList<>();
    }

    public abstract boolean needScaling(Service service);

    public abstract void scaling(Service service);


}
