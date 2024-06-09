package policy.cloudletScheduler;

import core.CloudNativeSim;
import core.Status;
import entity.Instance;
import entity.NativeCloudlet;
import extend.NativePe;
import extend.NativeVm;

import java.util.*;

public class NativeCloudletSchedulerBestEffort extends NativeCloudletScheduler {

    public NativeCloudletSchedulerBestEffort() {
        super();
    }

    private double getShareRequests(NativeCloudlet cloudlet, Instance processor){
        double needShare = (double) cloudlet.getLen() / processor.getCurrentAllocatedMips() * 1024;
        cloudlet.setShare(needShare);
        return needShare;
    }

    @Override
    public void receiveCloudlets(List<NativeCloudlet> cloudlets, List<Instance> instanceList) {
        // 加入等待队列
        addToWaitingQueue(cloudlets);
        // 设置允许执行的队列
        List<NativeCloudlet> executingEnabled = new ArrayList<>();
        // 查询是否能加入执行队列
        for (NativeCloudlet cloudlet : cloudlets) {
            // 如果能分配到处理器，加入
            if (distributeCloudlet(cloudlet,instanceList)){
                addToProcessingQueue(cloudlet);
                executingEnabled.add(cloudlet);
            }
            // 不能入队就继续等待
            else cloudlet.addWitTime(waitStep);
        }
        // 更新队列和实例用量
        getWaitingQueue().removeAll(executingEnabled);
        instanceList.forEach(this::updateProcessorUsage);
    }


    public boolean distributeCloudlet(NativeCloudlet cloudlet, List<Instance> instanceList) {
        // 检查instance list非空
        if (instanceList == null || instanceList.isEmpty()) {
            throw new IllegalArgumentException("Instance list cannot be null or empty");
        }

        // 选择利用率最低的instance
        Instance selectedInstance = instanceList.stream()
                .min(Comparator.comparingDouble(Instance::getUtilizationOfCpu))
                .orElse(null);

        // 检查是否有足够的 CPU 份额
        if(selectedInstance.getCurrentAllocatedCpuShare() - selectedInstance.getUsedShare() > getShareRequests(cloudlet,selectedInstance)){
            cloudlet.bindToInstance(selectedInstance);
            return true;
        }

        return false;
    }



    // Move cloudlets from waiting to execution queue
    public void addToProcessingQueue(NativeCloudlet cloudlet) {
        // 加入执行队列
        getExecQueue().add(cloudlet);
        // 更新字段
        cloudlet.setStatus(Status.Processing);
        cloudlet.setStartExecTime(CloudNativeSim.clock());
        // 更新实例
        Instance processor = cloudlet.getInstance();
        processor.getProcessingCloudlets().add(cloudlet);
    }


    // Update processor's used share based on processing cloudlets
    private void updateProcessorUsage(Instance processor) {

        // 更新cpu share
        double totalUsedShare = processor.getProcessingCloudlets().stream()
                .mapToDouble(cl -> (double) cl.getLen() / processor.getCurrentAllocatedMips() * 1024)
                .sum();
        processor.setUsedShare((int) Math.ceil(totalUsedShare));

        // 更新ram
        int totalUsedRam = processor.getProcessingCloudlets().stream()
                .mapToInt(cl -> (int) cl.getSize())
                .sum();
        processor.setUsedRam(totalUsedRam);
    }


    @Override
    public void processCloudlets() {
        Iterator<NativeCloudlet> iterator = getExecQueue().iterator();

        while (iterator.hasNext()) {
            NativeCloudlet cl = iterator.next();
            Instance processor = cl.getInstance();
            double execTime = (double) cl.getLen() / processor.getCurrentAllocatedMips();
            cl.setExecTime(execTime);

            iterator.remove();
            getFinishedList().add(cl);
            processor.getProcessingCloudlets().remove(cl);
            processor.getCompletionCloudlets().add(cl);
            cl.setStatus(Status.Success);
        }
    }

}