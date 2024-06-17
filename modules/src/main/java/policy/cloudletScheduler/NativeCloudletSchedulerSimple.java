package policy.cloudletScheduler;

import core.CloudNativeSim;
import core.Exporter;
import core.Status;
import entity.Instance;
import entity.RpcCloudlet;

import java.util.*;


/**
 * Cloudlet scheduler implementation using Solid Share allocation policy.
 */
public class NativeCloudletSchedulerSimple extends NativeCloudletScheduler {

    final double solidShare = 30;  // Adjust this value as needed

    double waitStep = 0.06;// 这里是默认等待时间

    public NativeCloudletSchedulerSimple() {
        super();
    }

    private double getShareRequests(RpcCloudlet cloudlet, Instance processor){
        cloudlet.setShare(solidShare);
        return solidShare;
    }


    // 从等待队列调度到执行队列或者失败队列
    public void schedule(){
        // 设置结束等待的队列
        List<RpcCloudlet> waitingStop = new ArrayList<>();
        // 查询是否能加入执行队列
        for (RpcCloudlet cloudlet : getWaitingQueue()) {
            // 如果能分配到处理器，加入
            if (distributeCloudlet(cloudlet,getService().getInstanceList())){
                addToProcessingQueue(cloudlet);
                waitingStop.add(cloudlet);
            }
            // 不能入队就继续等待
            else {
                cloudlet.addWitTime(waitStep);
                // 饿死的cloudlet放入失败队列
                if (cloudlet.getWaitTime() >= cloudlet.getRequest().getSLO()) {
                    failedQueue.add(cloudlet);
                    waitingStop.add(cloudlet);
                }
            }
        }
        // 更新等待队列
        getWaitingQueue().removeAll(waitingStop);
    }

    // 分配合适的实例
    public boolean distributeCloudlet(RpcCloudlet cloudlet, List<Instance> instanceList) {
        // 检查instance list非空
        if (instanceList == null || instanceList.isEmpty()) {
            throw new IllegalArgumentException("Instance list cannot be null or empty");
        }
        //  选择份额最大的instance
//        Instance selectedInstance = instanceList.stream()
//                .max(Comparator.comparingDouble(Instance::getFreeShare))
//                .orElse(null);
        // 随机选取实例
        Random random = new Random();
        Instance selectedInstance = instanceList.get(random.nextInt(instanceList.size()));

        // 检查是否有足够的 CPU 份额
        double shareRequests = getShareRequests(cloudlet,selectedInstance);
        if(selectedInstance.getFreeShare() >= shareRequests){
            cloudlet.bindToInstance(selectedInstance);
            // 更新实例用量
            selectedInstance.addUsedShare(shareRequests);
            selectedInstance.addUsedRam(cloudlet.getSize());
            return true;
        }
        return false;
    }


    // Move cloudlets from waiting to execution queue
    public void addToProcessingQueue(RpcCloudlet cloudlet) {
        // 加入执行队列
        getExecQueue().add(cloudlet);
        // 更新字段
        cloudlet.setStatus(Status.Processing);
        cloudlet.setStartExecTime(CloudNativeSim.clock());
        // 更新实例
        Instance processor = cloudlet.getInstance();
        processor.getProcessingCloudlets().add(cloudlet);
    }



    // 处理执行队列
    @Override
    public void processCloudlets() {
        // 去重集合
        Set<Instance> processors = new HashSet<>();

        List<RpcCloudlet> completed = new ArrayList<>();
        // 记录stage的持续时间
        double stageSession = 0;
        // 执行
        for (RpcCloudlet cl :getExecQueue()){
            Instance processor = cl.getInstance();
            double execTime = (double) cl.getLen() / processor.getCurrentAllocatedMips();
            cl.setExecTime(execTime);
            stageSession = Math.max(stageSession,execTime);
            completed.add(cl);
            processors.add(processor);

        }

        // 报告处理时间内的processor的用量历史
        double currentTime = CloudNativeSim.clock();
        double finalStageSession = stageSession;
        processors.forEach(p -> Exporter.updateUsageHistory(p,currentTime, finalStageSession));

        // 结束队列
        getExecQueue().removeAll(completed);
        getFinishedList().addAll(completed);
        for (RpcCloudlet cl : completed){
            // 释放资源
            cl.getInstance().releaseCloudlet(cl);
            cl.setStatus(Status.Success);
        }
    }

}
