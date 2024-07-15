/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.scaling;

import core.Exporter;
import entity.Instance;
import entity.Service;
import extend.NativeVm;
import extend.UsageData;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static core.Reporter.printEvent;

@Getter
@Setter
public class VerticalScalingPolicy extends ServiceScalingPolicy{
    // 范围，小于下界缩容，大于上界扩容
    private double[] cpuThreshold = {0.5,0.8};

    private double targetCpuUtilization = 0.65;

    private Map<String, List<UsageData>> usageOfCpuHistory = Exporter.usageOfCpuHistory;
    // 最近10条记录
    private int recentRange = 10;

    private Set<Instance> scalingSet = new HashSet<>();
    private List<Instance> failedList = new ArrayList<>();
    private List<Instance> finishedList = new ArrayList<>();

    public VerticalScalingPolicy() {
        super();
    }

    @Override
    public boolean needScaling(Service service) {
        boolean flag = false;
        for (Instance instance : service.getInstanceList()) {
            // 读取用量历史
            List<UsageData> recentData = usageOfCpuHistory.get(instance.getUid());
            if (recentData == null) continue;
            int size = recentData.size();
            double recentUsage = recentData.subList(Math.max(0,size-recentRange), size).stream()
                    .mapToDouble(UsageData::getUsage).average().orElse(0.0); // 近期平均值
            double recentUtilization = recentUsage / instance.getCurrentAllocatedCpuShare();
            if (recentUtilization > cpuThreshold[1] || recentUtilization < cpuThreshold[0]){
                // 放入scaling
                getScalingSet().add(instance);
                flag =  true;
            }
        }
        return flag;
    }

    @Override
    public void scaling(Service service) {//TODO: 怎么总是缩容呢？

        for (Instance instance : getScalingSet()) {

            double cpuUsed = instance.getCurrentUsedCpuShare();
            int cpuAllocated = instance.getCurrentAllocatedCpuShare();
            assert cpuAllocated != 0: "the share of " + instance.getName() + " is empty.";
            // 计算伸缩后的资源
            int cpuRequests = (int) Math.ceil(cpuUsed / targetCpuUtilization);
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

        getScalingSet().removeAll(getFinishedList());
        getFinishedList().clear();
        getNewInstances().clear();

    }

}
