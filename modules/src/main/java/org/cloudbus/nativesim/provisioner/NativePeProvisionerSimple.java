/*
 * Copyright ©2023. Jingfeng Wu.
 */

package org.cloudbus.nativesim.provisioner;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.nativesim.entity.NativeEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class NativePeProvisionerSimple extends NativePeProvisioner{
    private Map<String, List<Double>> peTable;
    protected void setPeTable(Map<String, ? extends List<Double>> peTable) {
        this.peTable = (Map<String, List<Double>>) peTable;
    }
    public NativePeProvisionerSimple(double availableMips) {
        super(availableMips);
        setPeTable(new HashMap<String, ArrayList<Double>>());
    }

    @Override
    public boolean allocateMipsForEntity(NativeEntity entity, double mips) {
        return allocateMipsForEntity(entity.getUid(), mips);
    }

    @Override
    public boolean allocateMipsForEntity(String entityUid, double mips) {
        if (getAvailableMips() < mips) {
            return false;
        }

        List<Double> allocatedMips;

        if (getPeTable().containsKey(entityUid)) {
            allocatedMips = getPeTable().get(entityUid);
        } else {
            allocatedMips = new ArrayList<Double>();
        }

        allocatedMips.add(mips);

        setAvailableMips(getAvailableMips() - mips);
        getPeTable().put(entityUid, allocatedMips);

        return true;
    }

    @Override
    public boolean allocateMipsForEntity(NativeEntity entity, List<Double> mips) {
        int totalMipsToAllocate = 0;
        for (double _mips : mips) {
            totalMipsToAllocate += _mips;
        }

        if (getAvailableMips() + getTotalAllocatedMipsForEntity(entity) < totalMipsToAllocate) {
            return false;
        }

        setAvailableMips(getAvailableMips() + getTotalAllocatedMipsForEntity(entity) - totalMipsToAllocate);

        getPeTable().put(entity.getUid(), mips);

        return true;
    }

    @Override
    public List<Double> getAllocatedMipsForEntity(NativeEntity entity) {
        if (getPeTable().containsKey(entity.getUid())) {
            return getPeTable().get(entity.getUid());
        }
        return null;
    }

    @Override
    public double getTotalAllocatedMipsForEntity(NativeEntity entity) {
        if (getPeTable().containsKey(entity.getUid())) {
            double totalAllocatedMips = 0.0;
            for (double mips : getPeTable().get(entity.getUid())) {
                totalAllocatedMips += mips;
            }
            return totalAllocatedMips;
        }
        return 0;
    }

    @Override
    public double getAllocatedMipsForEntityByVirtualPeId(NativeEntity entity, int peId) {
        if (getPeTable().containsKey(entity.getUid())) {
            try {
                return getPeTable().get(entity.getUid()).get(peId);
            } catch (Exception e) {
            }
        }
        return 0;
    }

    @Override
    public void deallocateMipsForEntity(NativeEntity entity) {
        if (getPeTable().containsKey(entity.getUid())) {
            for (double mips : getPeTable().get(entity.getUid())) {
                setAvailableMips(getAvailableMips() + mips);
            }
            getPeTable().remove(entity.getUid());
        }
    }

}
