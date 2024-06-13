package policy.cloudletScheduler;

import core.CloudNativeSim;
import core.Exporter;
import core.Status;
import entity.Instance;
import entity.NativeCloudlet;
import extend.NativePe;
import extend.NativeVm;

import java.util.*;

public class NativeCloudletSchedulerBestEffort extends NativeCloudletSchedulerSimple {

    public NativeCloudletSchedulerBestEffort() {
        super();
    }

    // best-effort 计算
    private double getShareRequests(NativeCloudlet cloudlet, Instance processor){
        double needShare = (double) cloudlet.getLen() / processor.getCurrentAllocatedMips() * 1024;
        cloudlet.setShare(needShare);
        return needShare;
    }



    public boolean distributeCloudlet(NativeCloudlet cloudlet, List<Instance> instanceList) {
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


}