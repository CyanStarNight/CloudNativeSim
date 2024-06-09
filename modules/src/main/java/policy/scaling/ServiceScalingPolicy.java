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

    // 准备scaling
    private List<Instance> scalingList;
    // scaling失败
    private List<Instance> failedList;

    private List<Instance> finishedList;

    private List<Instance> replications;


    public ServiceScalingPolicy() {
        scalingList = new ArrayList<Instance>();
        failedList = new ArrayList<Instance>();
        finishedList = new ArrayList<Instance>();
        replications = new ArrayList<Instance>();
    }

    public ServiceScalingPolicy(Service service,ServiceAllocationPolicy serviceAllocationPolicy) {
        this.vmList = serviceAllocationPolicy.getVmList();
        failedList = new ArrayList<Instance>();
        finishedList = new ArrayList<Instance>();
        replications = new ArrayList<Instance>();
    }

    public abstract boolean needScaling(Service service);

    public abstract void scaling(Service service);

}
