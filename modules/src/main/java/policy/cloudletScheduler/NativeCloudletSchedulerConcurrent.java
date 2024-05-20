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
public class NativeCloudletSchedulerConcurrent extends NativeCloudletScheduler {

    public NativeCloudletSchedulerConcurrent() {
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
        // 遍历等待队列，检查是否可以将 cloudlet 移至执行队列
        Iterator<NativeCloudlet> iterator = getWaitingQueue().iterator();
        while (iterator.hasNext()) {
            NativeCloudlet cloudlet = iterator.next();
            Instance processor = cloudlet.getInstance();

            // 计算 cloudlet 需要的 share
            double needShare = (double) cloudlet.getLen() / processor.getCurrentAllocatedMips() * 1024;
            cloudlet.setShare(needShare);
            // 检查 processor 是否有足够的空余资源
            if (processor.getCurrentAllocatedCpuShare() - processor.getUsedShare() > needShare) {
                // 从等待队列中移除
                iterator.remove();

                // 加入执行队列
                getExecQueue().add(cloudlet);
                cloudlet.setStatus(Status.Processing);
                processor.getProcessingCloudlets().add(cloudlet);
                processor.totalCloudlets += 1;

                // 更新 processor 已用 share
                updateProcessorUsedShare(processor);
            } else {
                // 如果不能加入执行队列，增加等待时间
                cloudlet.addWitTime(waitStep);
            }
        }
    }

    private void updateProcessorUsedShare(Instance processor) {
        // 重新计算所有正在处理的 cloudlets 的 needShare 总和
        double totalUsedShare = processor.getProcessingCloudlets().stream()
                .mapToDouble(cl -> (double) cl.getLen() / processor.getCurrentAllocatedMips() * 1024)
                .sum();

        processor.setUsedShare((int) Math.ceil(totalUsedShare));
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
