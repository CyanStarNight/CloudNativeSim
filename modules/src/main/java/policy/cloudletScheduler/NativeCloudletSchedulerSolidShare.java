package policy.cloudletScheduler;

import core.Status;
import entity.Instance;
import entity.NativeCloudlet;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Cloudlet scheduler implementation using Solid Share allocation policy.
 */
public class NativeCloudletSchedulerSolidShare extends NativeCloudletScheduler {

    final double solidShare = 20.0;  // Adjust this value as needed

    public NativeCloudletSchedulerSolidShare() {
        super();
    }

    /**
     * Distribute cloudlets to instances based on the Solid Share policy.
     * @param nativeCloudlets List of native cloudlets to be distributed.
     * @param instanceList List of available instances.
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
            addToWaitingQueuq(nativeCloudlet);

            usedRam[randomIndex] += nativeCloudlet.getSize();
            counts[randomIndex] += 1;
        }
        for (int i = 0; i < instanceList.size(); i++) {
            if (counts[i] > 0) {
                instanceList.get(i).setUsedRam((int) (usedRam[i] / counts[i]));
            } else {
                instanceList.get(i).setUsedRam(0);
            }
        }
    }

    /**
     * Add cloudlets to the processing queue based on available resources.
     */
    public void addToProcessingQueue() {
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
                updateProcessorUsedShare(processor);
            } else {
                cloudlet.addWitTime(waitStep);
            }
        }
    }

    /**
     * Update the used share of the processor based on processing cloudlets.
     * @param processor Instance to update.
     */
    private void updateProcessorUsedShare(Instance processor) {
        double totalUsedShare = processor.getProcessingCloudlets().stream()
                .mapToDouble(NativeCloudlet::getShare)
                .sum();

        processor.setUsedShare((int) Math.ceil(totalUsedShare));
    }

    @Override
    public void processCloudlets() {
        addToProcessingQueue();

        Iterator<NativeCloudlet> iterator = getExecQueue().iterator();
        while (iterator.hasNext()) {
            NativeCloudlet cl = iterator.next();
            assert cl.getInstance() != null; // Ensure each cloudlet is allocated to an instance
            Instance processor = cl.getInstance();
            double execTime = (double) cl.getLen() / processor.getCurrentAllocatedMips();

            cl.setExecTime(execTime);
            iterator.remove();
            getFinishedList().add(cl);
            processor.getProcessingCloudlets().remove(cl);
            cl.status = Status.Success;
        }
    }

}
