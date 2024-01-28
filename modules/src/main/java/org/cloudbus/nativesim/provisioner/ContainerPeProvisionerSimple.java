/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import org.cloudbus.nativesim.entity.Instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ContainerPeProvisionerSimple extends InstancePeProvisioner {
    private Map<String, List<Double>> containerPeTable;
    public ContainerPeProvisionerSimple(double availableMips) {
        super(availableMips);
        setContainerPeTable(new HashMap<String, ArrayList<Double>>());
    }

    @Override
    public boolean allocateMipsForInstance(Instance container, double mips) {
        return allocateMipsForInstance(container.getUid(), mips);
    }

    @Override
    public boolean allocateMipsForInstance(String containerUid, double mips) {
        if (getAvailableMips() < mips) {
            return false;
        }

        List<Double> allocatedMips;

        if (getContainerPeTable().containsKey(containerUid)) {
            allocatedMips = getContainerPeTable().get(containerUid);
        } else {
            allocatedMips = new ArrayList<Double>();
        }

        allocatedMips.add(mips);

        setAvailableMips(getAvailableMips() - mips);
        getContainerPeTable().put(containerUid, allocatedMips);

        return true;
    }

    @Override
    public boolean allocateMipsForInstance(Instance container, List<Double> mips) {
        int totalMipsToAllocate = 0;
        for (double _mips : mips) {
            totalMipsToAllocate += _mips;
        }

        if (getAvailableMips() + getTotalAllocatedMipsForInstance(container) < totalMipsToAllocate) {
            return false;
        }

        setAvailableMips(getAvailableMips() + getTotalAllocatedMipsForInstance(container) - totalMipsToAllocate);

        getContainerPeTable().put(container.getUid(), mips);

        return true;
    }

    @Override
    public List<Double> getAllocatedMipsForInstance(Instance container) {
        if (getContainerPeTable().containsKey(container.getUid())) {
            return getContainerPeTable().get(container.getUid());
        }
        return null;
    }

    @Override
    public double getTotalAllocatedMipsForInstance(Instance container) {
        if (getContainerPeTable().containsKey(container.getUid())) {
            double totalAllocatedMips = 0.0;
            for (double mips : getContainerPeTable().get(container.getUid())) {
                totalAllocatedMips += mips;
            }
            return totalAllocatedMips;
        }
        return 0;
    }

    @Override
    public double getAllocatedMipsForInstanceByVirtualPeId(Instance container, int peId) {
        if (getContainerPeTable().containsKey(container.getUid())) {
            try {
                return getContainerPeTable().get(container.getUid()).get(peId);
            } catch (Exception e) {
            }
        }
        return 0;
    }

    @Override
    public void deallocateMipsForInstance(Instance container) {
        if (getContainerPeTable().containsKey(container.getUid())) {
            for (double mips : getContainerPeTable().get(container.getUid())) {
                setAvailableMips(getAvailableMips() + mips);
            }
            getContainerPeTable().remove(container.getUid());
        }
    }

    protected Map<String, List<Double>> getContainerPeTable() {
        return containerPeTable;
    }

    @SuppressWarnings("unchecked")
    protected void setContainerPeTable(Map<String, ? extends List<Double>> containerPeTable) {
        this.containerPeTable = (Map<String, List<Double>>) containerPeTable;
    }

}
