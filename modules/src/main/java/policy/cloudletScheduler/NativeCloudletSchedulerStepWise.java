package policy.cloudletScheduler;

import core.Status;
import entity.Instance;
import entity.NativeCloudlet;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Cloudlet scheduler that employs a step-wise share allocation method.
 */
public class NativeCloudletSchedulerStepWise extends NativeCloudletScheduler {

    final double solidShare = 20.0;  // Adjust this value according to your needs

    public NativeCloudletSchedulerStepWise() {
        super();
    }

    /**
     * Distributes cloudlets among instances using a random selection method.
     * @param nativeCloudlets List of cloudlets to be distributed.
     * @param instanceList List of instances available for distribution.
     */
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

    /**
     * Adds eligible cloudlets to the processing queue based on available resources.
     */
    public void addToProcessingQueue() {
        updateAllInstancesUsedShare(instanceList);

        Iterator<NativeCloudlet> iterator = getWaitingQueue().iterator();
        while (iterator.hasNext()) {
            NativeCloudlet cloudlet = iterator.next();
            Instance processor = cloudlet.getInstance();

            cloudlet.setShare(solidShare);

            if (processor.getCurrentAllocatedCpuShare() - processor.getUsedShare() > solidShare) {
                iterator.remove();
                getExecQueue().add(cloudlet);
                cloudlet.setStatus(Status.Processing);
                processor.getProcessingCloudlets().add(cloudlet);
                processor.totalCloudlets += 1;
            } else {
                cloudlet.addWitTime(waitStep);
            }
        }
    }

    /**
     * Updates the used share for all instances based on the total cloudlets they are processing.
     * @param instanceList List of all instances.
     */
    /**
     * Updates the used share for all instances based on the total cloudlets they are processing.
     * @param instanceList List of all instances.
     */
    public void updateAllInstancesUsedShare(List<Instance> instanceList) {
        if (instanceList.isEmpty()) return;

        int totalCloudlets = instanceList.stream().mapToInt(Instance::getTotalCloudlets).sum();
        if (totalCloudlets == 0) {
            instanceList.forEach(instance -> instance.setUsedShare(1)); // Set a minimal possible value
            return;
        }

        double maxShareValue = 50.0; // Set maximum share value
        instanceList.forEach(instance -> {
            double proportion = (double) instance.getTotalCloudlets() / totalCloudlets;
            int mappedValue = 1 + (int) (proportion * (maxShareValue - 1)); // Map the proportion to a range from 1 to maxShareValue

            // Apply an exponential function to increase differentiation
            int usedShare = (int) Math.pow(2, mappedValue / 10.0);
            instance.setUsedShare(usedShare);
        });
    }


    @Override
    public void processCloudlets() {
        addToProcessingQueue();

        Iterator<NativeCloudlet> iterator = getExecQueue().iterator();
        while (iterator.hasNext()) {
            NativeCloudlet cl = iterator.next();
            assert cl.getInstance() != null;
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
        // TODO: Implement logic to pause cloudlets
    }

    @Override
    public void resumeCloudlets() {
        // TODO: Implement logic to resume paused cloudlets
    }
}
