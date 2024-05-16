/*
 * Copyright ©2024. Jingfeng Wu.
 */

package provisioner;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import entity.Instance;

@Getter
@Setter
@NoArgsConstructor
public abstract class VmBwProvisioner {

    private double totalBw; //total bw;

    private double availableReceiveBw;

    private double availableTransmitBw;

    public void init(double totalBw) {
        setTotalBw(totalBw);
        setAvailableReceiveBw(totalBw);
        setAvailableTransmitBw(totalBw);
    }

    public abstract boolean allocateBwForInstance(Instance instance, double receiveBw, double transmitBw);

    public abstract Double getAllocatedReceiveBwForInstance(Instance instance);

    public abstract Double getAllocatedTransmitBwForInstance(Instance instance);

    public abstract void deallocateBwForInstance(Instance instance);

    public void deallocateBwForAllInstances() {
        setAvailableReceiveBw(getTotalBw());
    }
    public abstract boolean isSuitableForInstance(Instance instance, double receiveBw, double transmitBw);

    public double getUsedReceiveBw() {
        return totalBw - availableReceiveBw;
    }

    public double getUsedTransmitBw() {
        return totalBw - availableTransmitBw;
    }

}
