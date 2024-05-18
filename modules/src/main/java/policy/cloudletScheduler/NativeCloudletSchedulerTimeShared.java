/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.cloudletScheduler;

import core.Status;
import entity.Instance;
import entity.NativeCloudlet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sareh on 10/07/15.
 */
public class NativeCloudletSchedulerTimeShared extends NativeCloudletScheduler {

    public NativeCloudletSchedulerTimeShared(List<Instance> instanceList) {
        super(instanceList);
    }

    // 分发cloudlets到该服务的实例上
    public void distributeCloudlet(NativeCloudlet nativeCloudlet, List<Instance> instanceList) {
        // 按cloudlet数量重新从小到大排序
        instanceList.sort(Comparator.comparingInt(i -> getWaitingQueue().size()));
        Instance selectedInstance = instanceList.get(0);
        nativeCloudlet.setInstanceUid(selectedInstance.getUid());
        receiveCloudlet(nativeCloudlet);
        getCloudletsMap().get(selectedInstance.getUid()).add(nativeCloudlet.getId());
    }

    /* 顺序处理cloudlets */
    @Override
    public double processCloudlets() {
        // 收集所有待处理的cloudlets
        List<NativeCloudlet> toProcess = new ArrayList<>(getWaitingQueue());
        double totalExecTime = 0;

        for (NativeCloudlet cl : toProcess) {
            // 从等待列表中移除
            getWaitingQueue().remove(cl);
            // 添加到执行列表
            getExecQueue().add(cl);
            // 检查是否已经部署
            assert cl.getInstance() != null;
            int len = cl.getLen();
            // 计算执行时间
            double execTime = (double) len / cl.getInstance().getCurrentAllocatedMips();
            cl.setExecTime(execTime);
            totalExecTime += execTime;
            // 从执行列表中移除，并添加到完成列表
            getExecQueue().remove(cl);
            getFinishedList().add(cl);
            cl.status = Status.Success;
            cl.getRequest().addDelay(execTime);
        }
        return totalExecTime;
    }

    @Override
    public void pauseCloudlets() {
        // TODO: 实现暂停cloudlets的逻辑
    }

    @Override
    public void resumeCloudlets() {
        // TODO: 实现恢复cloudlets的逻辑
    }

    @Override
    public double getTotalUtilizationOfCpu(double time) {
        // TODO: 需要实现实际的CPU利用率计算逻辑
        return 0;
    }

    @Override
    public double getTotalUtilizationOfRam(double time) {
        // TODO: 需要实现实际的内存利用率计算逻辑
        return 0;
    }

    @Override
    public double getTotalUtilizationOfBw(double time) {
        // TODO: 需要实现实际的带宽利用率计算逻辑
        return 0;
    }
}