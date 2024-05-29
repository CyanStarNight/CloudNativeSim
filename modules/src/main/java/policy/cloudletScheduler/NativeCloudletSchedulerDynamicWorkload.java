package policy.cloudletScheduler;

import core.Status;
import entity.Instance;
import entity.NativeCloudlet;

import java.util.*;

public class NativeCloudletSchedulerDynamicWorkload extends NativeCloudletScheduler {

    public NativeCloudletSchedulerDynamicWorkload() {
        super();
    }

    // Distribute cloudlets across instances
    public void distributeCloudlets(List<NativeCloudlet> nativeCloudlets, List<Instance> instanceList) {
        if (instanceList == null || instanceList.isEmpty()) {
            throw new IllegalArgumentException("Instance list cannot be null or empty");
        }

        Random random = new Random();
        double[] usedRam = new double[instanceList.size()];
        int[] counts = new int[instanceList.size()];

        for (NativeCloudlet nativeCloudlet : nativeCloudlets) {
            int randomIndex = random.nextInt(instanceList.size());
            Instance selectedInstance = instanceList.get(randomIndex);
            nativeCloudlet.setInstanceUid(selectedInstance.getUid());
            receiveCloudlets(nativeCloudlet);

            usedRam[randomIndex] += nativeCloudlet.getSize();
            counts[randomIndex]++;
        }

        for (int i = 0; i < instanceList.size(); i++) {
            instanceList.get(i).setUsedRam(counts[i] > 0 ? (int) (usedRam[i] / counts[i]) : 0);
        }
    }

    // Move cloudlets from waiting to execution queue
    public void addToProcessingQueue() {
        Iterator<NativeCloudlet> iterator = getWaitingQueue().iterator();
        while (iterator.hasNext()) {
            NativeCloudlet cloudlet = iterator.next();
            Instance processor = cloudlet.getInstance();

            double needShare = (double) cloudlet.getLen() / processor.getCurrentAllocatedMips() * 1024;
            cloudlet.setShare(needShare);
            if (processor.getCurrentAllocatedCpuShare() - processor.getUsedShare() > needShare) {
                iterator.remove();
                getExecQueue().add(cloudlet);
                cloudlet.setStatus(Status.Processing);
                processor.getProcessingCloudlets().add(cloudlet);
                processor.totalCloudlets++;
                updateProcessorUsedShare(processor);
            } else {
                cloudlet.addWitTime(waitStep);
            }
        }
    }

    // Update processor's used share based on processing cloudlets
    private void updateProcessorUsedShare(Instance processor) {
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
            Instance processor = cl.getInstance();
            double execTime = (double) cl.getLen() / processor.getCurrentAllocatedMips();
            cl.setExecTime(execTime);

            iterator.remove();
            getFinishedList().add(cl);
            processor.getProcessingCloudlets().remove(cl);
            cl.setStatus(Status.Success);
        }
    }

}