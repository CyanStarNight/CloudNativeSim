/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.scaling;

import entity.Instance;
import entity.ReplicaSet;
import entity.Service;
import extend.NativeVm;

import java.util.List;

public class HorizontalScalingPolicy extends ServiceScalingPolicy{

    double cpuThreshold = 0.8;

    public HorizontalScalingPolicy() {
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
            // 避免扩展
            if (cpuUsed == 0) continue;
            // 水平扩展
            Instance replica =instance.clone(); // 自动加入replica中
            // 尝试分配
            assert replica.getRequests_share() == instance.getRequests_share();

            if (getServiceAllocationPolicy().allocateVmForInstance(replica)){
                getReplications().add(replica);
                service.getInstanceList().add(replica);
                System.out.println(replica.getCurrentAllocatedMips());
            }
            else {
                getFailedList().add(replica);
            }
            getFinishedList().add(replica);
//            if (flag) printEvent(instance.getPrefix() + " has been horizontally scaled.);
        }
        getScalingList().removeAll(getFinishedList());
        getFinishedList().clear();
    }
}
