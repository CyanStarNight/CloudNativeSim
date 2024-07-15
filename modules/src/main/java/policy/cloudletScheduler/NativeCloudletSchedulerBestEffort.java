package policy.cloudletScheduler;

import entity.Instance;
import entity.RpcCloudlet;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class NativeCloudletSchedulerBestEffort extends NativeCloudletSchedulerSimple {

    public NativeCloudletSchedulerBestEffort() {
        super();
    }


    // best-effort 计算
    protected double getShareRequests(RpcCloudlet cloudlet, Instance processor){
        double needShare = (double) cloudlet.getLen() / processor.getCurrentAllocatedMips() * 1024;

        cloudlet.setShare(needShare);
        return needShare;
    }

    // 分配合适的实例
    //TODO: 部署失败的cloudlet似乎没有得到很好的处理
    public boolean distributeCloudlet(RpcCloudlet cloudlet, List<Instance> instanceList) {
        boolean flag = false;
        // 检查instance list非空
        if (instanceList == null)
            throw new IllegalArgumentException("Instance list cannot be null.");

        if (instanceList.isEmpty())
            throw new IllegalArgumentException("Instance list cannot be empty.");
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
            flag = true;
        }
        return flag;
    }

}