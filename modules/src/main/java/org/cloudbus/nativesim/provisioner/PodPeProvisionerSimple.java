/*
 * Copyright ©2024. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.nativesim.entity.Instance;
import org.cloudbus.nativesim.entity.Pod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PodPeProvisionerSimple extends InstancePeProvisioner {
    private Map<String, List<Double>> podPeTable;
    private final String instanceType = "Pod";

    public PodPeProvisionerSimple(double mips) {
        super(mips);
        setPodPeTable(new HashMap<String, List<Double>>());
    }
//TODO: 2024/1/24 与ram、bw不同，pe在模拟中不会实际分配给container，直接交给pod
    @Override
    public boolean allocateMipsForInstance(Instance pod, double mips) {
        return allocateMipsForInstance(pod.getUid(), mips);
    }

    @Override
    public boolean allocateMipsForInstance(String podUid, double mips) {
        if (getAvailableMips() < mips) {
            return false;
        }

        List<Double> podAllocatedMips;

        if (getPodPeTable().containsKey(podUid)) {
            podAllocatedMips = getPodPeTable().get(podUid);
        } else {
            podAllocatedMips = new ArrayList<Double>();
        }

        podAllocatedMips.add(mips);

        setAvailableMips(getAvailableMips() - mips);
        getPodPeTable().put(podUid, podAllocatedMips);

        return true;
    }

    @Override
    public boolean allocateMipsForInstance(Instance pod, List<Double> mips) {
        int totalMipsToAllocate = 0;
        for (double _mips : mips) {
            totalMipsToAllocate += _mips;
        }

        if (getAvailableMips() + getTotalAllocatedMipsForInstance(pod) < totalMipsToAllocate) {
            return false;
        }

        setAvailableMips(getAvailableMips() + getTotalAllocatedMipsForInstance(pod) - totalMipsToAllocate);

        getPodPeTable().put(pod.getUid(), mips);

        return true;
    }

    @Override
    public List<Double> getAllocatedMipsForInstance(Instance pod) {
        if (getPodPeTable().containsKey(pod.getUid())) {
            return getPodPeTable().get(pod.getUid());
        }
        return null;
    }

    @Override
    public double getTotalAllocatedMipsForInstance(Instance pod) {
        if (getPodPeTable().containsKey(pod.getUid())) {
            double totalAllocatedMips = 0.0;
            for (double mips : getPodPeTable().get(pod.getUid())) {
                totalAllocatedMips += mips;
            }
            return totalAllocatedMips;
        }
        return 0;
    }

    @Override
    public double getAllocatedMipsForInstanceByVirtualPeId(Instance pod, int peId) {
        if (getPodPeTable().containsKey(pod.getUid())) {
            try {
                return getPodPeTable().get(pod.getUid()).get(peId);
            } catch (Exception e) {
            }
        }
        return 0;
    }

    @Override
    public void deallocateMipsForInstance(Instance pod) {
        if (getPodPeTable().containsKey(pod.getUid())) {
            for (double mips : getPodPeTable().get(pod.getUid())) {
                setAvailableMips(getAvailableMips() + mips);
            }
            getPodPeTable().remove(pod.getUid());
        }
    }

    protected Map<String, List<Double>> getPodPeTable() {
        return podPeTable;
    }

    @SuppressWarnings("unchecked")
    protected void setPodPeTable(Map<String, ? extends List<Double>> podPeTable) {
        this.podPeTable = (Map<String, List<Double>>) podPeTable;
    }


}
