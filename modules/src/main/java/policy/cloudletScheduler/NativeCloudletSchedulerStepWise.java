/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.cloudletScheduler;

import core.Status;
import entity.Instance;
import entity.NativeCloudlet;

import java.util.Iterator;
import java.util.List;
import java.util.Random;


/**
 * Created by sareh on 10/07/15.
 */
public class NativeCloudletSchedulerStepWise extends NativeCloudletScheduler {

    final double solidShare = 20.0;  // 你可以根据实际需要调整这个值

    public NativeCloudletSchedulerStepWise() {
        super();
    }


    public void distributeCloudlets(List<NativeCloudlet> nativeCloudlets, List<Instance> instanceList) {
        if (instanceList == null || instanceList.isEmpty()) {
            throw new IllegalArgumentException("Instance list cannot be null or empty");
        }
        Random random = new Random();  // 创建 Random 实例用于选择 Instance

        double[] usedRam = new double[instanceList.size()];
        int[] counts = new int[instanceList.size()];
        for (NativeCloudlet nativeCloudlet : nativeCloudlets) {
            // 随机选择一个 Instance
            int randomIndex = random.nextInt(instanceList.size());
            Instance selectedInstance = instanceList.get(randomIndex);

            // 设置 cloudlet 的 Instance UID
            nativeCloudlet.setInstanceUid(selectedInstance.getUid());

            // 将 cloudlet 加入到等待队列
            receiveCloudlets(nativeCloudlet);

            // 根据 cloudlet 的大小增加对应 Instance 的已用 RAM
            usedRam[randomIndex] += nativeCloudlet.getSize();
            counts[randomIndex] += 1;
        }
        for (int i = 0; i < instanceList.size(); i++) {
            if (counts[i] > 0) { // 确保防止除以0
                instanceList.get(i).setUsedRam((int) (usedRam[i] / counts[i]));
            } else {
                instanceList.get(i).setUsedRam(0); // 如果没有接收到任何 Cloudlet，可以设为0或其他适当处理
            }
        }
    }

    public void addToProcessingQueue() {
        // 更新所有实例的 usedShare
        updateAllInstancesUsedShare(instanceList);  // 假设 getAllInstances() 返回所有的 instance 列表

        // 遍历等待队列，检查是否可以将 cloudlet 移至执行队列
        Iterator<NativeCloudlet> iterator = getWaitingQueue().iterator();
        while (iterator.hasNext()) {
            NativeCloudlet cloudlet = iterator.next();
            Instance processor = cloudlet.getInstance();

            // 固定的 share，不需要计算
            cloudlet.setShare(solidShare);

            // 检查 processor 是否有足够的空余资源
            if (processor.getCurrentAllocatedCpuShare() - processor.getUsedShare() > solidShare) {
                // 从等待队列中移除
                iterator.remove();

                // 加入执行队列
                getExecQueue().add(cloudlet);
                cloudlet.setStatus(Status.Processing);
                processor.getProcessingCloudlets().add(cloudlet);
                processor.totalCloudlets += 1;
            } else {
                // 如果不能加入执行队列，增加等待时间
                cloudlet.addWitTime(waitStep);
            }
        }
    }



    public void updateAllInstancesUsedShare(List<Instance> instanceList) {
        if (instanceList.isEmpty()) return;  // 如果列表为空，直接返回

        // 计算所有 instances 的 totalCloudlets 总和
        int totalCloudlets = instanceList.stream().mapToInt(Instance::getTotalCloudlets).sum();

        // 如果 totalCloudlets 为 0，避免除零错误
        if (totalCloudlets == 0) {
            instanceList.forEach(instance -> instance.setUsedShare(1));  // 设置最小可能值
            return;
        }

        // 计算每个 instance 的 share 分配值
        instanceList.forEach(instance -> {
            // 计算每个 instance 的比例并映射到 [1, 10] 区间
            double proportion = (double) instance.getTotalCloudlets() / totalCloudlets;
            int mappedValue = 1 + (int) (proportion * 9); // 将比例映射到 1 到 10 的区间

            // 计算 2 的 mappedValue 次幂
            int usedShare = (int) Math.pow(2, mappedValue);
            instance.setUsedShare(usedShare);
        });
    }




    @Override
    public void processCloudlets() {
        addToProcessingQueue();

        Iterator<NativeCloudlet> iterator = getExecQueue().iterator();
        while (iterator.hasNext()) {
            NativeCloudlet cl = iterator.next();
            assert cl.getInstance() != null; // 确保每个 cloudlet 都已分配给一个实例
            Instance processor = cl.getInstance();
            double execTime = (double) cl.getLen() / processor.getCurrentAllocatedMips();

            cl.setExecTime(execTime);
            iterator.remove();
            getFinishedList().add(cl);
            processor.getProcessingCloudlets().remove(cl);
            cl.status = Status.Success;
        }

    }

    @Override
    public void pauseCloudlets() {
        // TODO: 实现暂停cloudlets的逻辑
    }

    @Override
    public void resumeCloudlets() {
        // TODO: 实现恢复cloudlets的逻辑
    }
}
