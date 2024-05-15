/*
 * Copyright ©2024. Jingfeng Wu.
 */

package provisioner;

import lombok.Getter;
import lombok.Setter;
import service.Instance;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class VmBwProvisionerSimple extends VmBwProvisioner {

    private Map<Instance, Double> instanceReceiveBwTable;
    private Map<Instance, Double> instanceTransmitBwTable;
    private final String instanceType = "Instance";

    public VmBwProvisionerSimple() {
        setInstanceReceiveBwTable(new HashMap<>());
        setInstanceTransmitBwTable(new HashMap<>());
    }
    @Override
    public boolean allocateBwForInstance(Instance instance, double receiveBw, double transmitBw) {

        deallocateBwForInstance(instance);

        if (getAvailableReceiveBw() >= receiveBw && getAvailableTransmitBw() >= transmitBw) {
            // 分配接收带宽
            setAvailableReceiveBw(getAvailableReceiveBw() - receiveBw);
            getInstanceReceiveBwTable().put(instance, receiveBw);
            instance.setCurrentAllocatedReceiveBw(getAllocatedReceiveBwForInstance(instance));

            // 分配传输带宽
            setAvailableTransmitBw(getAvailableTransmitBw() - transmitBw);
            getInstanceTransmitBwTable().put(instance, transmitBw);
            instance.setCurrentAllocatedTransmitBw(getAllocatedTransmitBwForInstance(instance));

            return true;
        }

        instance.setCurrentAllocatedReceiveBw(getAllocatedReceiveBwForInstance(instance));
        instance.setCurrentAllocatedTransmitBw(getAllocatedTransmitBwForInstance(instance));

        return false;
    }

    @Override
    public Double getAllocatedReceiveBwForInstance(Instance instance) {

        if (getInstanceReceiveBwTable().containsKey(instance)) {

            return getInstanceReceiveBwTable().get(instance);
        }

        return 0.0;
    }

    @Override
    public Double getAllocatedTransmitBwForInstance(Instance instance) {

        if (getInstanceTransmitBwTable().containsKey(instance)) {

            return getInstanceTransmitBwTable().get(instance);
        }

        return 0.0;
    }

    @Override
    public void deallocateBwForInstance(Instance instance) {
        if (getInstanceReceiveBwTable().containsKey(instance)) {
            double amountFreed = getInstanceReceiveBwTable().remove(instance);
            setAvailableReceiveBw(getAvailableReceiveBw() + amountFreed);
            instance.setCurrentAllocatedReceiveBw(0);
        }
    }

    public void deallocateBwForAllInstances() {

        setAvailableReceiveBw(getTotalBw());
        getInstanceReceiveBwTable().clear();

        setAvailableTransmitBw(getTotalBw());
        getInstanceTransmitBwTable().clear();
    }

    @Override
    public boolean isSuitableForInstance(Instance instance, double receiveBw, double transmitBw) {

        double allocatedReceiveBw = getAllocatedReceiveBwForInstance(instance);
        double allocatedTransmitBw = getAllocatedReceiveBwForInstance(instance);

        boolean result = allocateBwForInstance(instance, receiveBw,transmitBw);

        deallocateBwForInstance(instance);

        if (allocatedReceiveBw > 0 && allocatedTransmitBw > 0) {
            allocateBwForInstance(instance, allocatedReceiveBw, allocatedTransmitBw);
        }

        return result;
    }

}
