/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.scaling;

import entity.Instance;
import entity.Service;
import extend.NativeVm;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static core.Reporter.printEvent;

@Getter
@Setter
public class VerticalScalingPolicy extends ServiceScalingPolicy{

    double cpuThreshold = 0.8;

    private List<Instance> scalingList = new ArrayList<>();
    private List<Instance> failedList = new ArrayList<>();
    private List<Instance> finishedList = new ArrayList<>();

    public VerticalScalingPolicy() {
        super();
    }

    @Override
    public boolean needScaling(Service service) {
        boolean flag = false;
        for (Instance instance : service.getInstanceList()) {
            if (instance.getUtilizationOfCpu() > cpuThreshold){
                // 放入scaling
                getScalingList().add(instance);
                flag =  true;
            }
        }
        return flag;
    }

    @Override
    public void scaling(Service service) {//TODO: 缩容处理似乎有问题

        for (Instance instance : getScalingList()) {
            // 计算伸缩后的资源
            int cpuUsed = instance.getUsedShare();
            int cpuAllocated = instance.getCurrentAllocatedCpuShare();
            int cpuRequests = (int) Math.ceil(cpuUsed / cpuThreshold);
            // 避免扩展
            if (cpuUsed == 0) continue;
            // 更新requests
            instance.setRequests_share(cpuRequests);
            // 查询vm列表,是否有充足资源
            NativeVm selectedVm = null;
            // 能否直接扩展
            assert instance.getCurrentAllocatedPe() != null;
            int available = instance.getCurrentAllocatedPe().getAvailableShare();
            if (available >= cpuRequests - cpuAllocated)
                selectedVm = instance.getVm();
            // 需要重新分配vm
            else {
                for (NativeVm vm : getVmList())
                    if (vm.getMaxFreeShare() >= cpuRequests) selectedVm = vm;
            }
            if (selectedVm == null) {
                getFailedList().add(instance);
                continue;
            }
            // 释放实例的资源
//            for (Service service : instance.getServiceList()) service.deleteInstance(instance);
            getServiceAllocationPolicy().deallocateVmForInstance(instance);
            // 重新部署
            boolean flag = getServiceAllocationPolicy().allocateVmForInstance(instance,selectedVm);

            if (flag) {
                getFinishedList().add(instance);
                printEvent("the share of " + instance.getName() + " has been vertically scaled to "+cpuRequests);
            }
        }

        getScalingList().removeAll(getFinishedList());
        getFinishedList().clear();

    }

}
